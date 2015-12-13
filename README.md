# How to use

Add submodule from git to you project:

	git submodule add https://github.com/PNixx/Lists-Android.git
	
Add to `settings.gradle` file:

	include ':Lists-Android'
    
Add to `build.gradle` file on your app:

	dependencies {
		...
		compile project(path: ':Lists-Android')
	}
