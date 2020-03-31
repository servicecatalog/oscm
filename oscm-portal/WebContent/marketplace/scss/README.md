**What is Sass?**

Sass is a **CSS pre-processor**. It lets you compile **.sass** and **.scss** files into **.css** code (the .scss format is preferably used, as it is an extension of the usual CSS syntax with Sass features).

The css code can therefore become more re-usable.

##**Steps of Installation of Sass with Ruby.**

1. Download RubyInstaller  for Windows from the page: https://rubyinstaller.org/downloads/
2. Select recommended version: **Ruby+Devkit 2.6.X (x64)**
3. Follow the Installation Wizard steps. Be sure to select the Option &quot;Add Ruby Executables to Path&quot; and continue.
4. Once finished open up a Command Prompt and type: `ruby –v`, to confirm that Ruby has been successfully installed.
5. Install Sass with the command: `gem install sass`.
6. Confirm that Sass has been successfully installed by typing the command: `sass –v`.

##**Integrate CSS-Preprocessig with Eclipse**

###**Part 1 - Associate the .scss file type with the native Eclipse CSS Editor**

1. Go to Window \&gt; Preferences
2. Drill down to General \&gt; Editors \&gt; File Associations
3. In File Associations pane, click the &#39;Add...&quot; button on the top right.
4. For File Type:, enter \*.scss and then click OK.
5. Find the \*.scss entry in the File Associations list and select it.
6. After selecting \*.scss, on the bottom pane Associated editors:, click the .. button.
7. Make sure Internal editors is selected on the top, then find and select CSS Editor and then click OK.

This associated the file type .scss with eclipses native CSS Editor. Now we have to configure the native CSS Editor to support .scss files. To do this, follow this steps:

###**Part 2 - Add the .scss file type to the native CSS Editor**

1. Go to Window \&gt; Preferences
2. Drill down to General \&gt; Content Types
3. In the Content Types pane, expand Text, then select CSS
4. After CSS is selected, on the bottom File associations: pane, click the .. button.
5. For Content type:, enter \*.scss and then click OK.
6. Click OK to close out the Preferences window.

Note: If the css colours do not appear you may have to do the following: Right click the .scss file \&gt; Open With \&gt; CSS Editor.

###**Part 3 - Configure a Sass Builder in Eclipse:**

1. From the Project menu select &quot;Properties&quot; and choose the &quot;Builders&quot; section.
2. Create a new Builder and select &quot;Program&quot; as configuration type.
3. Choose a name for your launch configuration, e.g. &quot;Sass\_Builder&quot;.
4. Insert the path of your sass installation into the Location field (e.g. &quot;C:\Ruby26-x64\bin\sass.bat&quot;.
5. Use ${project\_loc} as working directory.
6. In the &quot;Build Options&quot; tab just check all options under the &quot;Run the builder:&quot; section. You can also &quot;Specify working set of relevant resources&quot; to launch the builder only when files contained in selected folders are saved.
7. In the Arguments text box insert the following:

`--update WebContent/marketplace/scss:WebContent/marketplace/css`

 (The --update parameter defines the directory source of your sass files followed by &quot;:&quot; and the destination folder for the compiled css files. In my configuration &quot;scss&quot; is the source folder containing the .scss files and &quot;css&quot; is the destination directory containing the compiled .css files. The --update command will check for modifications in the source folder and all sub-folders.)

You can also optionally type `--style-compressed` in order to compress the resulting stylesheet.

1. Click Ok to save your launching configuration.
2. Now try to modify a .scss file in your source directory and then save it, you 'll see the sass CLI output in your console window.

In case the css files are not present or the .scss files have no changes, the scss will be compiled into css.

