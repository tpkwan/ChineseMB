document.addEventListener("deviceready", onDeviceReady, false);

//Activate :active state
document.addEventListener("touchstart", function() {
}, false);

function onDeviceReady() {
	var app = new Application();
	app.run();
}

function Application() {
}

Application.prototype = {
    
	run: function() {
		var that = this;
		that.openExternalInAppBrowser;
/*
		var that = this,
		openLocal = document.getElementById("openLocal"),
		openExternalInAppBrowser = document.getElementById("openExternalInAppBrowser"),
		openPDF = document.getElementById("openPDF"),
		openExternalPDF = document.getElementById("openExternalPDF"),
		openExternalInSystemBrowser = document.getElementById("openExternalInSystemBrowser");
		openLocal.addEventListener("click", that.openLocal);		
		openExternalInAppBrowser.addEventListener("click", that.openExternalInAppBrowser);
		openPDF.addEventListener("click", that.openPDF);
		openExternalPDF.addEventListener("click", that.openExternalPDF);
		openExternalInSystemBrowser.addEventListener("click", that.openExternalInSystemBrowser);
*/        
	},
    
	openLocal: function() {
		window.open("img/ice.png", "_blank");
	},
 
	openExternalInAppBrowser:  function () {
		window.open("http://mbcca.ca", "_blank");
	},
    
	openExternalPDF:  function () {
		if (device.uuid == "e0101010d38bde8e6740011221af335301010333" || device.uuid == "e0908060g38bde8e6740011221af335301010333") {
			alert("Not Supported in Simulator.");
		}
		else {
			if (device.platform == 'Android') {
				window.open("http://samplepdf.com/sample.pdf", "_system");
			}
			else
				window.open("http://samplepdf.com/sample.pdf", "_blank");
		}
	},
	
	openPDF: function() {
		if (device.uuid == "e0101010d38bde8e6740011221af335301010333" || device.uuid == "e0908060g38bde8e6740011221af335301010333") {
			alert("Not Supported in Simulator.");
		}
		else {
			if (device.platform == 'Android') {
				window.open("whitepaper-mobile-developer-guidance.pdf", "_system");
			}
			else
				window.open("whitepaper-mobile-developer-guidance.pdf", "_blank");
		}
	},
    
	openExternalInSystemBrowser:function () {
		window.open("http://wiki.apache.org/cordova/InAppBrowser", "_system");
	}
}