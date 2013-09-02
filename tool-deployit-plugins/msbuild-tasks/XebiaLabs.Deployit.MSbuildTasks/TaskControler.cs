using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Text;
using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;
using XebiaLabs.Deployit.Client;
using XebiaLabs.Deployit.Client.Manifest;
using XebiaLabs.Deployit.Client.Package;
using XebiaLabs.Deployit.Client.UDM;

namespace XebiaLabs.Deployit.MSbuildTasks
{
    public class TaskControler : DeployitConnectedTask
    {
        [Required]
        public string TaskId { get; set; }

        [Required]
        public string Action { get; set; }

        protected override bool ExecuteCore(DeployitServer deployitServer)
        {
            var taskService = deployitServer.TaskService;

            switch (Action.ToLowerInvariant())
            {
                case "start":
                    taskService.Start(TaskId);
                    break;
                case "stop":
                    taskService.Stop(TaskId);
                    break;
                case "abort":
                    taskService.Stop(TaskId);
                    break;
                case "cancel":
                    taskService.Cancel(TaskId);
                    break;
                case "archive":
                    taskService.Archive(TaskId);
                    break;
                default:
                    Log.LogError("unknown task action: {0}", Action);
                    return false;

            }
            return true;
        }
    }
}
