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

namespace XebiaLabs.Deployit.MSbuildTasks
{
	public abstract class DeployitConnectedTask : Task
	{
		[Required]
		public string DeployitURL { get; set; }

		[Required]
		public string Username { get; set; }

		[Required]
		public string Password { get; set; }

		
		public bool CheckConnection { get; set; }

	   
		protected abstract bool ExecuteCore(DeployitServer deployitServer);


		public override bool Execute()
		{
			string errorMessage;
			var server = new DeployitServer();
			var connectionStatus = server.Connect(new Uri(DeployitURL), new NetworkCredential(Username, Password), out errorMessage, CheckConnection);
			if (connectionStatus != ConnectionStatus.Connected)
			{
				Log.LogError("Connection failed to DeployIt: {0}", errorMessage);
				return false;
			}
			try
			{
				return ExecuteCore(server);
			}
			catch (Exception ex)
			{                
				Log.LogErrorFromException(ex);
				return false;
			}
			finally
			{
				server.Disconnect();
			}
		}
	}

}