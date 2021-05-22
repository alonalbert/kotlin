/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

#include "ThreadData.hpp"
#include "ThreadState.hpp"
#include "ThreadSuspensionUtils.hpp"

#include <thread>

namespace {

// TODO: Accept a ThreadSuspensionData?
bool isSuspendedOrNative(kotlin::mm::ThreadData& thread) {
    auto& suspensionData = thread.suspensionData();
    return suspensionData.suspended() || suspensionData.state() == kotlin::ThreadState::kNative;
}

bool isRunnableOrNative(kotlin::mm::ThreadData& thread) {
    auto state = thread.state();
    return state == kotlin::ThreadState::kRunnable || state == kotlin::ThreadState::kNative;
}

template<typename F>
bool allThreads(F predicate) {
    auto& threadRegistry = kotlin::mm::ThreadRegistry::Instance();
    auto* currentThread = threadRegistry.CurrentThreadData();
    kotlin::mm::ThreadRegistry::Iterable threads = threadRegistry.Iter();
    for (auto& thread : threads) {
        // Handle if suspension was initiated by the mutator thread.
        if (&thread == currentThread)
            continue;
        if (!predicate(thread)) {
            return false;
        }
    }
    return true;
}

void yield() {
    std::this_thread::yield();
}

std::atomic<bool> gSuspensionRequested = false;
THREAD_LOCAL_VARIABLE bool gSuspensionRequestedByCurrentThread = false;
std::mutex gSuspensionMutex;
std::condition_variable gSuspendsionCondVar;

} // namespace

bool kotlin::mm::ThreadSuspensionData::suspendIfRequested() noexcept {
    if (IsThreadSuspensionRequested()) {
        std::unique_lock lock(gSuspensionMutex);
        if (IsThreadSuspensionRequested()) {
            suspended_ = true;
            gSuspendsionCondVar.wait(lock, []() { return !IsThreadSuspensionRequested(); });
            suspended_ = false;
            return true;
        }
    }
    return false;
}

bool kotlin::mm::IsThreadSuspensionRequested() {
    // TODO: Consider using a more relaxed memory order.
    return gSuspensionRequested.load();
}

bool kotlin::mm::SuspendThreads() {
    RuntimeAssert(gSuspensionRequestedByCurrentThread == false, "Current thread already suspended threads:");

    bool actual = false;
    gSuspensionRequested.compare_exchange_strong(actual, true);
    if (actual == true) {
        return false;
    }
    gSuspensionRequestedByCurrentThread = true;

    // Spin wating for threads to suspend. Ignore Native threads.
    while(!allThreads(isSuspendedOrNative)) {
        yield();
    }

    return true;
}

void kotlin::mm::ResumeThreads() {
    // From the std::condition_variable docs:
    // Even if the shared variable is atomic, it must be modified under
    // the mutex in order to correctly publish the modification to the waiting thread.
    // https://en.cppreference.com/w/cpp/thread/condition_variable
    {
        std::unique_lock lock(gSuspensionMutex);
        gSuspensionRequested = false;
    }
    gSuspensionRequestedByCurrentThread = false;
    gSuspendsionCondVar.notify_all();

    // Wait for threads to run. Ignore Native threads.
    // TODO: This loop (+ GC lock) allows us to avoid the situation when a resumed thread triggers the GC again while we still resuming other threads.
    //       Try to get rid of this?
    while(!allThreads(isRunnableOrNative)) {
        yield();
    }
}
