using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;
using XebiaLabs.Deployit.Client.Manifest;
using XebiaLabs.Deployit.Client.Package;

namespace XebiaLabs.Deployit.MSbuildTasks
{
    public class CreatePackage : Task
    {
        /*
         * 
         *     [RequiredArgument]
        public InArgument<string> ManifestPath { get; set; }

        [RequiredArgument]
        public InArgument<string> ApplicationVersion { get; set; }

        [RequiredArgument]
        public InArgument<string> PackageDataRootPath { get; set; }

        [RequiredArgument]
        public InArgument<string> PackagePath { get; set; }

        public OutArgument<string> ApplicationName { get; set; }
         * 
         * 
         */
        [Required]
        public string ManifestPath { get; set; }

        [Required]
        public string ApplicationVersion { get; set; }

        [Required]
        public string PackageDataRootDirectory { get; set; }

        [Required]
        public string PackagePath { get; set; }

        [Output]
        public string ApplicationName { get; set; }

        public override bool Execute()
        {


            if (!File.Exists(ManifestPath))
            {
                Log.LogError("Cannont find manifest '{0}", ManifestPath);
                return false;
            }
            
            if (string.IsNullOrWhiteSpace(ApplicationVersion))
            {
                Log.LogError("Version not provided");
                return false;
            }

            if (!Directory.Exists(PackageDataRootDirectory))
            {
                Log.LogError("Package root directory'{0}' does not exist", PackageDataRootDirectory);
                return false;
            }
           
            DeployitManifest manifest;
            try
            {
                var reader = new StreamReader(ManifestPath, DeployitManifest.Encoding);
                using (reader)
                {
                    manifest = DeployitManifest.Load(reader);
                }
            }
            catch (Exception ex)
            {
                Log.LogErrorFromException(ex);
                return false;
            }
            manifest.Version = ApplicationVersion;

            ApplicationName = manifest.ApplicationName;

            var packageBuilder = new PackageGenerator();
            packageBuilder.GeneratePackage(manifest, PackageDataRootDirectory, PackagePath);
            return true;
        }          
    }
}