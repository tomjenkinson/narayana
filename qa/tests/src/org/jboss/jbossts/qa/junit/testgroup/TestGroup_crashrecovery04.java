/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.junit.testgroup;

import org.jboss.jbossts.qa.junit.*;
import org.junit.*;

// Automatically generated by XML2JUnit
public class TestGroup_crashrecovery04 extends TestGroupBase
{
	public String getTestGroupName()
	{
		return "crashrecovery04";
	}

	protected Task server0 = null;

	@Before public void setUp()
	{
		super.setUp();
		server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
	}

	@After public void tearDown()
	{
		try {
			server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform("$(1)");
		} finally {
			super.tearDown();
		}
	}

	@Test public void CrashRecovery04_Test01()
	{
		setTestName("Test01");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test02()
	{
		setTestName("Test02");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server02.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test03()
	{
		setTestName("Test03");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server02.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client03.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test04()
	{
		setTestName("Test04");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client04.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test05()
	{
		setTestName("Test05");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client05.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test06()
	{
		setTestName("Test06");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client06.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test07()
	{
		setTestName("Test07");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client07.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test08()
	{
		setTestName("Test08");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server02.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client08.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test09()
	{
		setTestName("Test09");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server02.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client09.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test10()
	{
		setTestName("Test10");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client10.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test11()
	{
		setTestName("Test11");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client11.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test12()
	{
		setTestName("Test12");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client12.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test13()
	{
		setTestName("Test13");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.CrashRecovery04Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client13.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test14()
	{
		setTestName("Test14");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server02.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.CrashRecovery04Servers.Server02.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client14.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test15()
	{
		setTestName("Test15");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server02.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.CrashRecovery04Servers.Server02.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client15.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test16()
	{
		setTestName("Test16");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.CrashRecovery04Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client16.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test17()
	{
		setTestName("Test17");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.CrashRecovery04Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client17.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test18()
	{
		setTestName("Test18");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.CrashRecovery04Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client18.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test19()
	{
		setTestName("Test19");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server04.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client13.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test20()
	{
		setTestName("Test20");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server05.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client14.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test21()
	{
		setTestName("Test21");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server05.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client15.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test22()
	{
		setTestName("Test22");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server06.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client16.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test23()
	{
		setTestName("Test23");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server06.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client17.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test24()
	{
		setTestName("Test24");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server06.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client18.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test25()
	{
		setTestName("Test25");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server07.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test26()
	{
		setTestName("Test26");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server08.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test27()
	{
		setTestName("Test27");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server08.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client03.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test28()
	{
		setTestName("Test28");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client04.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test29()
	{
		setTestName("Test29");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client05.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test30()
	{
		setTestName("Test30");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client06.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test31()
	{
		setTestName("Test31");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server07.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client07.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test32()
	{
		setTestName("Test32");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server08.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client08.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test33()
	{
		setTestName("Test33");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server08.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client09.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test34()
	{
		setTestName("Test34");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client10.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test35()
	{
		setTestName("Test35");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client11.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test36()
	{
		setTestName("Test36");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client12.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test37()
	{
		setTestName("Test37");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server07.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.CrashRecovery04Servers.Server07.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client13.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test38()
	{
		setTestName("Test38");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server08.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.CrashRecovery04Servers.Server08.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client14.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test39()
	{
		setTestName("Test39");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server08.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.CrashRecovery04Servers.Server08.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client15.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test40()
	{
		setTestName("Test40");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.CrashRecovery04Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client16.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test41()
	{
		setTestName("Test41");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.CrashRecovery04Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client17.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test42()
	{
		setTestName("Test42");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.CrashRecovery04Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client18.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test43()
	{
		setTestName("Test43");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server10.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client13.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test44()
	{
		setTestName("Test44");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server11.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client14.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test45()
	{
		setTestName("Test45");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server11.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client15.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test46()
	{
		setTestName("Test46");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server12.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client16.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test47()
	{
		setTestName("Test47");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server12.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client17.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void CrashRecovery04_Test48()
	{
		setTestName("Test48");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.CrashRecovery04Servers.Server12.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.CrashRecovery04Clients.Client18.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server1.terminate();
	}

}