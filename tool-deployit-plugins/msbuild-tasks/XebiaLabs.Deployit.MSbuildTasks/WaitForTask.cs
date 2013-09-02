using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading;
using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;
using XebiaLabs.Deployit.Client;
using XebiaLabs.Deployit.Client.Manifest;
using XebiaLabs.Deployit.Client.Package;

namespace XebiaLabs.Deployit.MSbuildTasks
{
	public class WaitForTask : DeployitConnectedTask
	{
		[Required]
		public string TaskId { get; set; }

		[Output]
		public string FinalState { get; set; }

		public int RefreshIntervalInSeconds { get; set; }

		public WaitForTask()
		{
			RefreshIntervalInSeconds = 10;
		}

		protected override bool ExecuteCore(DeployitServer deployitServer)
		{
			var taskServer = deployitServer.TaskService;
			string taskState;
			do
			{
				Thread.Sleep(RefreshIntervalInSeconds * 1000);
				var info = taskServer.GetTaskInfo(TaskId);
				taskState = info.State;
				Log.LogMessage("Task '{0}' state: '{1}'", TaskId, taskState);
				
			}
			while (taskState == "QUEUED" || taskState == "EXECUTING");

			FinalState = taskState;
			return true;
		}

	}
}
