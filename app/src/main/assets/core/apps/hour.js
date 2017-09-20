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

hour = new App("hour", "Atual Hour");
hour.setApp(["#1622Atual Hour"], hour);

hour.importNumber(0);

hour.app = function(){
	view = new View;

	hour.onCreate = function(){
		var date = new Date;
		data = date.getHours() + ":" + date.getMinutes();

		view.setString(28, 16, data);
		hour.setView(view);
	}

	hour.onCreate();
}