/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.processors.cache.persistence.defragmentation;

import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.internal.mem.unsafe.UnsafeMemoryProvider;
import org.apache.ignite.internal.pagemem.FullPageId;
import org.apache.ignite.internal.pagemem.PageIdAllocator;
import org.apache.ignite.internal.pagemem.PageMemory;
import org.apache.ignite.internal.pagemem.impl.PageMemoryNoStoreImpl;
import org.apache.ignite.internal.processors.cache.persistence.DataRegionMetricsImpl;
import org.apache.ignite.internal.processors.cache.persistence.diagnostic.pagelocktracker.PageLockTrackerManager;
import org.apache.ignite.testframework.junits.GridTestKernalContext;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Class for LinkMap tests.
 */
public class LinkMapTest extends GridCommonAbstractTest {
    /** */
    protected static final int PAGE_SIZE = 512;

    /** */
    protected static final long MB = 1024 * 1024;

    /** */
    private final PageMemory pageMem = createPageMemory();

    /** {@inheritDoc} */
    @Override protected void beforeTest() throws Exception {
        super.beforeTest();

        pageMem.start();
    }

    /** {@inheritDoc} */
    @Override protected void afterTest() throws Exception {
        super.afterTest();

        pageMem.stop(true);
    }

    /**
     * Test that LinkMap works.
     * @throws Exception
     */
    @Test
    public void test() throws Exception {
        int cacheGroupId = 1;

        String groupName = "test";

        FullPageId pageId = new FullPageId(pageMem.allocatePage(cacheGroupId, 0, PageIdAllocator.FLAG_DATA), cacheGroupId);

        PageLockTrackerManager pageLockTrackerMgr = mock(PageLockTrackerManager.class);

        when(pageLockTrackerMgr.createPageLockTracker(anyString())).thenReturn(PageLockTrackerManager.NOOP_LSNR);

        LinkMap map = new LinkMap(cacheGroupId, groupName, pageMem, pageId.pageId(), true, pageLockTrackerMgr);

        for (int i = 0; i < 10_000; i++)
            map.put(i, i + 1);

        for (int i = 0; i < 10_000; i++)
            assertEquals(i + 1, map.get(i));
    }

    /**
     * Create page memory for LinkMap tree.
     */
    private static PageMemory createPageMemory() {
        DataRegionConfiguration plcCfg = new DataRegionConfiguration()
                .setInitialSize(2 * MB)
                .setMaxSize(2 * MB);

        return new PageMemoryNoStoreImpl(
            log,
            new UnsafeMemoryProvider(log),
            null,
            PAGE_SIZE,
            plcCfg,
            new DataRegionMetricsImpl(plcCfg, new GridTestKernalContext(log)),
            true
        );
    }
}
