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
    public class GenerateDeploymentTask : DeployitConnectedTask
    {
        [Required]
        public string EnvironmentName { get; set; }

        [Required]
        public string ApplicationName { get; set; }

        [Required]
        public string VersionId { get; set; }

        [Output]
        public string TaskId { get; set; }


        protected override bool ExecuteCore(DeployitServer deployitServer)
        {
            var repo = deployitServer.RepositoryService;
            var deploymentService = deployitServer.DeploymentService;

            string environmentId = string.Format("/Environments/{0}", EnvironmentName);
            string deploymentId = string.Format("/Environments/{0}/{1}", EnvironmentName, ApplicationName);

            Deployment d;
            var deployedExists = repo.Exists(deploymentId);

            if (!deployedExists)
            {
                d = deploymentService.PrepareInitial(VersionId, environmentId);
                d = deploymentService.GenerateAllDeployed(d);
            }
            else
            {
                d = deploymentService.PrepareUpdate(VersionId, deploymentId);
            }

            d = deploymentService.Validate(d);


            string taskId = deploymentService.GenerateDeploymentTask(d);

            TaskId = taskId;

            return true;
        }

    }
}