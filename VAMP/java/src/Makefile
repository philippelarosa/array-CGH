#
# CGH GUI Makefile
#
# Eric Viara for Institue Curie (c) 2003
#

CLASSDIR=../classes

#ALIAS = plarosa
#PASSWD = 123soleil
ALIAS = eviara
PASSWD = uuu123

#JAVAC = javac -Xmaxerrs 10000 #-deprecation
JAVAC = javac #-deprecation
#JAVAC = /opt/j2sdk_nb/jdk1.5.0/bin/javac -Xlint:deprecation
#JAVAC = /opt/j2sdk_nb/jdk1.5.0/bin/javac

TEST_PLUGIN_DIR = test-plugin

VAMPJAR = $(CLASSDIR)/VAMPAppli.jar
VAMPSIGNJAR = $(CLASSDIR)/VAMPApplet.jar

all: clean VAMPAppli $(VAMPSIGNJAR) $(VAMPJAR) VAMP2Image ImportProfile ReadProfile CreateImages ComputeGraphicInfo

appli: clean VAMPAppli

$(VAMPSIGNJAR): VAMPApplet
	(cd ../classes; jar cvf ../src/$@ fr/curie/vamp; jarsigner -storepass $(PASSWD) -keystore $$HOME/.cghaia-keystore ../src/$@ $(ALIAS))

$(VAMPJAR): VAMPAppli
	(cd ../classes; jar cvf ../src/$@ fr/curie/vamp)

.PHONY: oracle.jar

oracle.jar:
	jarsigner -storepass $(PASSWD) -keystore $$HOME/.cghaia-keystore $@ $(ALIAS)

plugin.jar: PluginTest
	(cd ../classes; jar cf ../src/$@ *.class; jarsigner -storepass $(PASSWD) -keystore $$HOME/.cghaia-keystore ../src/$@ $(ALIAS))

PluginTest:
	$(JAVAC) -d $(CLASSDIR) $(TEST_PLUGIN_DIR)/*.java

VAMPAppli:
	time $(JAVAC) -d $(CLASSDIR) fr/curie/vamp/$@.java

VAMP2Image:
	time $(JAVAC) -d $(CLASSDIR) fr/curie/vamp/$@.java

VAMPApplet:
	$(JAVAC) -d $(CLASSDIR) fr/curie/vamp/$@.java

ImportProfile ReadProfile CreateImages ComputeGraphicInfo:
	$(JAVAC) -d $(CLASSDIR) fr/curie/vamp/tools/$@.java

pack_only:
	(version=`grep "static int VERSION" fr/curie/vamp/VersionManager.java | awk '/VERSION_V/ {printf("%s.", $$6); next;} /VERSION_R/ {printf("%s.", $$6); next;} /VERSION_M/ {printf("%s\n", $$6); exit;}' - | sed 's/;//g'`; \
         dir=`pwd`; \
         tarfile=VAMP-$$version.tar; \
         tmpdir=/tmp/VAMP-$$version; \
         mkdir $$tmpdir; \
         ln -s `pwd`/scripts $$tmpdir/; \
         cd ..; ln -s `pwd`/classes $$tmpdir; \
         cd /tmp; tar cvf $$tarfile `find VAMP-$$version -follow \( -name \*.class -o -name \*.jar -o -name \*sh \)`; \
         mv /tmp/$$tarfile $$dir/; \
         rm -r $$tmpdir; \
         echo VAMP package: $$tarfile)

pack: all pack_only

key:
	@echo "NOTE: you must input $(PASSWD) as keystore password"
	@echo
	keytool -genkey -alias $(ALIAS) -keystore $$HOME/.cghaia-keystore

license:
	javac -d ../classes fr/curie/vamp/LicenseManager.java fr/curie/vamp/LicenseLib.java fr/curie/vamp/LicenseParams.java

run_license:
	CLASSPATH=../classes java fr.curie.vamp.LicenseManager

VAMP_LICENSE_TAR = VAMPLicenseManager.tar 
VAMP_LICENSE_DIR = VAMPLicense

pack_license: license
	@(cd ..; if [ ! -d $(VAMP_LICENSE_DIR) ] ; then mkdir $(VAMP_LICENSE_DIR); fi ; find runlicense LICENSE-README.txt classes/fr/curie/vamp/LicenseManager.class classes/fr/curie/vamp/LicenseLib.class classes/fr/curie/vamp/LicenseParams.class | cpio -pduv $(VAMP_LICENSE_DIR))
	@(cd ..; tar cf $(VAMP_LICENSE_TAR) $(VAMP_LICENSE_DIR))
	@echo tar file is $(VAMP_LICENSE_TAR)

javadoc:
	javadoc -d ../javadoc fr/curie/vamp/*.java

VAMP_PLUGIN_ROOT_DIR = .
VAMP_PLUGIN_BASE_DIR = vamp_plugin
VAMP_PLUGIN_DIR = $(VAMP_PLUGIN_ROOT_DIR)/$(VAMP_PLUGIN_BASE_DIR)

pack_plugin: plugin.jar javadoc
	@rm -rf $(VAMP_PLUGIN_DIR)
	@if [ ! -d $(VAMP_PLUGIN_DIR) ]; then mkdir -p $(VAMP_PLUGIN_DIR)/src/$(TEST_PLUGIN_DIR)/cgi-bin; fi
	@cp plugin.jar $(TEST_PLUGIN_DIR)/Makefile $(TEST_PLUGIN_DIR)/*.java $(TEST_PLUGIN_DIR)/*.html $(VAMP_PLUGIN_DIR)/src/$(TEST_PLUGIN_DIR)/
	@cp $(TEST_PLUGIN_DIR)/cgi-bin/*.xml $(TEST_PLUGIN_DIR)/cgi-bin/*.php $(VAMP_PLUGIN_DIR)/src/$(TEST_PLUGIN_DIR)/cgi-bin/
	@(cd  $(VAMP_PLUGIN_DIR)/src/$(TEST_PLUGIN_DIR)/; for i in *.java; do ln -s $$i $$i.txt; done)
	(pwd=`pwd`; cd ..; find javadoc | cpio -pdu $$pwd/$(VAMP_PLUGIN_DIR))
	@(version=`grep "static int VERSION" fr/curie/vamp/VersionManager.java | awk '/VERSION_V/ {printf("%s_", $$6); next;} /VERSION_R/ {printf("%s_", $$6); next;} /VERSION_M/ {printf("%s\n", $$6); exit;}' - | sed 's/;//g'`; echo $$version >> $(VAMP_PLUGIN_DIR)/VERSION)
	@(cd $(VAMP_PLUGIN_ROOT_DIR); tar cf $(VAMP_PLUGIN_ROOT_DIR)/vamp_plugin.tar $(VAMP_PLUGIN_BASE_DIR))
	@echo "VAMP plugin documentation is available in file src/test-plugin/InstallPlugin.html and src/test-plugin/WritePlugin.html" > $(VAMP_PLUGIN_DIR)/README
	@rm -rf $(VAMP_PLUGIN_DIR)
	@echo Tarfile is $(VAMP_PLUGIN_ROOT_DIR)/vamp_plugin.tar

clean:
	find $(CLASSDIR) \( -name \*.class -o -name \*.jar \) -print | xargs rm -f
