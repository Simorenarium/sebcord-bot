package coffee.michel.sebcord.resources;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScriptResources {

	@GetMapping(path = "resources/gtag.js", produces = { "text/javascript" })
	public String getGtag() {
		return "(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){\r\n" +
				"(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),\r\n" +
				"m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)\r\n" +
				"})(window,document,'script','https://www.google-analytics.com/analytics.js','ga');\r\n" +
				"\r\n" +
				"ga('create', 'UA-159965600-1', 'auto');\r\n" +
				"ga('send', 'pageview');\r\n" +
				"\r\n" +
				"window.dataLayer = window.dataLayer || [];\r\n" +
				"\r\n" +
				"function gtag(){dataLayer.push(arguments);}\r\n" +
				"function deleteCookie( name ) {\r\n" +
				"  document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:01 GMT;';\r\n" +
				"}\r\n" +
				"function getCookie(name) {\r\n" +
				"  var value = \"; \" + document.cookie;\r\n" +
				"  var parts = value.split(\"; \" + name + \"=\");\r\n" +
				"  if (parts.length == 2) return parts.pop().split(\";\").shift();\r\n" +
				"}\r\n" +
				"\r\n" +
				"gtag('js', new Date());\r\n" +
				"gtag('config', 'UA-158165853-1');\r\n" +
				"console.log(\"Completed gscript\");\r\n" +
				"\r\n" +
				"\r\n" +
				"var userId = getCookie(\"sebcord.bot.userId\")\r\n" +
				"if(typeof userId !== 'undefined') {\r\n" +
				"	deleteCookie(\"sebcord.bot.userId\");\r\n" +
				"	gtag('set', {'user_id': userId}); // Set the user ID using signed-in user_id.\r\n" +
				"	ga('set', 'userId', userId); // Set the user ID using signed-in user_id.\r\n" +
				"	console.log(\"user id was set\");\r\n" +
				"}";
	}

}
