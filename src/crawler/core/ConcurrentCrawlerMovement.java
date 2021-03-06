/* 
 * The MIT License
 *
 * Copyright 2017 Michał Szymański, kontakt: michal.szymanski.aajar@gmail.com.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package crawler.core;

import crawler.data.Adress;
import crawler.utils.HumanFaker;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;
import michal.szymanski.util.collection.ClassGroupingMap;

/**
 *
 * @author Michał Szymański, kontakt: michal.szymanski.aajar@gmail.com
 */
public class ConcurrentCrawlerMovement extends FakerCrawlerMovement {

    public static final int MAX_THREADS = 10;
    private LinkedList<String> searchedURLS = new LinkedList();
    private LockCollection hostLocks = new LockCollection();
    private LockFutureTaskProcessor processor = new LockFutureTaskProcessor();
    private ExecutorService executors = Executors.newFixedThreadPool(MAX_THREADS);

    @Override
    protected void onStart() {
        super.onStart();
        executors.execute(() -> this.processor.start(super.getRemainingTime()));
    }

    @Override
    protected void onEnd() {
        super.onEnd();
        executors.shutdownNow();
    }

    private void downloadResults() {
        Optional<Entry<Lock, Object>> processorResult = processor.pollLastResult();
        if (!processorResult.isPresent()) {
            return;
        }
        ClassGroupingMap r =  (ClassGroupingMap) processorResult.get().getValue();
        results.putAll(r.toCollection());
        adresses.addAll(results.getGroup(Adress.class));
        freeLocks(processorResult.get().getKey());
    }

    private void freeLocks(Lock lock) {
        if (this.hostLocks.contains(lock)) {
            this.hostLocks.remove(lock);
        }

    }

    void createNewSearchThread(Adress adress) {
        Future future = executors.submit(()
                -> searchEngine.start(adress));

        Lock lock = new Lock(adress.getURL().getHost());
        processor.put(lock, (FutureTask) future);
        this.hostLocks.add(lock);
        this.searchedURLS.add(adress.get());

    }

    @Override
    Optional<Adress> findNextAdress() {
        downloadResults();
        Collection results = adresses.stream().filter((el) -> !this.hostLocks.contains(new Lock(el.getURL().getHost()))).collect(Collectors.toList());
        return Optional.ofNullable((Adress) HumanFaker.pollRandomElement(results));
    }

    @Override
    protected void doMove(Adress adress) {
        createNewSearchThread(adress);
    }

}
