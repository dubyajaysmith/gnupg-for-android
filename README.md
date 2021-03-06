# GNUPG for Android

**A port of the whole gnupg 2.1 suite to Android.**

## Target Platform

We would like to target as many Android platforms as possible.  Currently
there are two limiting APIs:

* regex:
    provided in Android 2.2, SDK android-8 and above
* pthread_rwlock\*:
    provided in Android 2.3, SDK android-9 and above

regex could easily be included in the build, pthread_rwlock\* would be more 
difficult.


## Build Setup

On **Debian/Ubuntu/Mint/etc.**:

	sudo apt-get install autoconf automake libtool transfig wget patch \
	texinfo ant gettext build-essential ia32-libs bison

On **Fedora 17 x64**:

	sudo yum install ncurses-libs.i686 libstdc++.i686 libgcc.i686 zlib.i686 gcc.i686

Install the Android NDK for the command line version, and the Android SDK for
the Android app version:

SDK: http://developer.android.com/sdk/
NDK: http://developer.android.com/sdk/ndk/

## Building

Update the git submodules:

	git submodule --init update

### How to Build the Command Line Utilities

To compile the components individually you can use commands like:

	make -C external/ gnupg-install
	make -C external/ gnupg-static
	make -C external/ gpgme-install

The results will be in `external/data/data/info.guardianproject.gpg`


### How to Build the Android Test App

	make -C external/ android-assets
	make -C external/ tests
	ndk-build
	android update project --path . --target android-8 \
	  --name GnuPrivacyGuard
	ant clean debug

### How to Build ALL THE THINGS (You want this one)

	make -C external/
	ndk-build
	android update project --path . --target android-8 \
	  --name GnuPrivacyGuard
	ant clean debug


# Testing

## pinentry

Testing pinentry is easiest on a rooted device

    adb shell
    $ su
    export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/data/data/info.guardianproject.gpg/app_opt/lib
    export PATH=$PATH:/data/data/info.guardianproject.gpg/app_opt/bin
    export HOME=/data/data/info.guardianproject.gpg/app_home
    export GNUPGHOME=/data/data/info.guardianproject.gpg/app_home
    # to test pinentry we want to import a secret key
    # start adb logcat in another terminal so you dont miss the action
    gpg2 --import /data/data/info.guardianproject.gpg/app_opt/tests/pinentry/secret-keys.gpg
