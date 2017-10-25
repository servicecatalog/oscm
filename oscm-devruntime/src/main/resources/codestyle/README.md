## Importing and configuring the code formatting rules and code templates

### Code formatting rules
* Download [codeformatting.xml](https://github.com/servicecatalog/development/blob/master/oscm-devruntime/javares/codestyle/codeformatting.xml)
* Start Eclipse and click on Window - Preferences
* Follow the tree to Java - Code Style - Formatter
* Click on Import and select the downloaded codeformatting.xml

### Code templates
* Download [codetemplate.xml](https://github.com/servicecatalog/development/blob/master/oscm-devruntime/javares/codestyle/codetemplate.xml)
* Start Eclipse and click on Window - Preferences
* Follow the tree to Java - Code Style - Code Templates
* Click on Import and select the downloaded codetemplate.xml
* Activate the checkbox "Automatically add comments for new methods and types"

### Formatting of non-java files
* Start Eclipse and click on Window - Preferences
* Follow the tree to 
  * Ant - Editor - Formatter
  * Web - HTML-Files - Editor
  * XML - XML-Files - Editor 
* Configure the following settings
  * Set the tab size to 2
  * Use spaces instead of tabs
  * Set the maximum line width to 120 