/* Copyright 2017 Yuri Faria

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. */

AsuraSystem = new App("AsuraSystem", "Asura System");

var importNumber = 0;
var logo = new Array;
var apps = new Array;

function AsuraSystemImportApps(apps){
	var appsArray = apps.substr(1, (apps.length - 2));
	appsArray = appsArray.split(', ');

	importNumber = appsArray.length;
	AsuraSystem.importNumber(importNumber);
	for(count = 0; count < importNumber; count++){
		AsuraSystem.import('apps/' + appsArray[count]);
	}
}

AsuraSystem.app = function(){
	var maxNv = importNumber - 1;
	var nv = 0;
	view = new View;

	this.onCreate = function(){
		view.setCustomArrayView(logo[nv]);
		this.setView(view);
	}

	this.onLeft = function(){
		if(nv != 0){
			nv--;
			view.setCustomArrayView(logo[nv]);
			this.setView(view);
			return;
		}

		nv = maxNv;
		view.setCustomArrayView(logo[nv]);
		this.setView(view);		
	}

	this.onRight = function(){
		if(nv != maxNv){
			nv++;
			view.setCustomArrayView(logo[nv]);
			this.setView(view);
		}
	}

	this.onDown = function(){
		this.startApp(apps[nv]);
	}

	this.onCreate();
}

this.getApps = function(logoAdd, appAdd){
	logo.push(logoAdd);
	apps.push(appAdd);
}
