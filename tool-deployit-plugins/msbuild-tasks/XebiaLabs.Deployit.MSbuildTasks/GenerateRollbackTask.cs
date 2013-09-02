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
    public class GenerateRollbackTask : DeployitConnectedTask
    {
        [Required]
        public string DeploymentTaskId { get; set; }


        [Output]
        public string RollbackTaskId { get; set; }

        protected override bool ExecuteCore(DeployitServer deployitServer)
        {
            var deploymentService = deployitServer.DeploymentService;

            string rollbackTaskId = deploymentService.GenerateRollbackTask(DeploymentTaskId);

            RollbackTaskId = rollbackTaskId;

            return true;
        }
    }
}