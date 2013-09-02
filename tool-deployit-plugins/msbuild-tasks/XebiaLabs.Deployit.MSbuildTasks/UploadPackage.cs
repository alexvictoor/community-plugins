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
    public class UploadPackage : DeployitConnectedTask
    {
        [Required]
        public string PackagePath { get; set; }

        [Output]
        public string PackageId { get; set; }

        protected override bool ExecuteCore(DeployitServer deployitServer)
        {
            var id = deployitServer.PackageService.Upload(PackagePath, Path.GetFileName(PackagePath));
            PackageId = id;
            return true;
        }
    }

}
