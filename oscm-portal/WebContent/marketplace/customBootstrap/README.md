# OSCM Customization Guide with Bootstrap

## Introduction
The new public marketplace UI has been re-designed based on the Bootstrap framework version 4.3.1.  
The default stylesheet for the marketplace: “mp.css” is still used for the customization of the marketplace UI, but now there are two additional Template Stylesheets, located in the “customBootstrap” folder: 1) a custom Bootstrap stylesheet (which is a full compiled & customized Bootstrap version) with a dark navigation bar named: “darkCustom.css”, and 2) a dark footer stylesheet, named “darkFooter.css”. The light versions of those stylesheets (named lightCustom.css, lightFooter.css) are also included in the branding package.

### Prerequisites:

-	You have downloaded the provided branding package. The branding package can be downloaded as a ZIP file in the administration portal as an Operator in the “Customize layout” page by clicking the button “Download branding package”. The branding package contains two folders containing Sass source files: the “scss” folder, containing source files of mp.css, and the “customBootstrap/scss” folder containing the theme source files for the custom Bootstrap, explained later on.
-	You have configured a Sass Compiler (a CSS pre-procesor) in order to compile your Sass source files with the file extension “.scss” and generate CSS stylesheets.  For more information on Sass and how to configure it, see the following README:  

-	https://github.com/servicecatalog/oscm/tree/master/oscm-portal/WebContent/marketplace/scss/README.md. A simple option is to use Ruby.
-	You need the downloaded Bootstrap source files v4.3.1 (let s assume that they are extracted in a folder named: “bootstrap/scss”)
-	The import statements in “darkCustom.scss” and “darkFooter.scss” must refer to the correct relative paths where the bootstrap source files are located.
Example of such an import statement:
 @import "../../../bootstrap/scss/variables";)
-	All generated css files (“darkCustom.css”, etc., explained in the next sections) are hosted on a server and preserve the same directory structure as in the provided branding package.
-	The URL of the branded mp.css is uploaded just like before in the "Customize Branding" page, and the location of “darkCustom.css” and “darkFooter.css” are automatically derived from the location of mp.css.



### Useful Links:
For more information about the Theming mechanism of Bootstrap via Sass in general, please refer to the source: https://getbootstrap.com/docs/4.3/getting-started/theming/ 
-	Another useful guide of how to customize Bootstrap is: 
-	http://bootstrap.themes.guide/how-to-customize-bootstrap.html

Another option is to use a Tool on the Internet to generate a Bootstrap template. There are several tools available that allow the easy and visual customization of Bootstrap UI components. Two examples of such tools are:
1.	Themestr.app themer or customizer: https://themestr.app/
2.	Bootstrap Builder: https://bootstrap.build/
There is also a list of pre-defined Bootstrap themes that are free to use and licensed under the MIT license:
https://bootswatch.com/
However, please note that these themes would not work out-of-the-box in the marketplace UI and would need to be adjusted.

## Customization Guide
The customization of the Bootstrap UI components is accomplished via Sass Variables. 
You can modify the variables located inside the file: _myVariables.scss and  then re-compile the sources to generate a modified “darkCustom.css”
These Sass variables override the default Bootstrap values. (Remember to remove the !default tag from them). 

### Base Sass Variables Template: _myVariables.scss
This file contains a minimum set of variables used for the Default Bootstrap Theme for the marketplace UI. It includes a green color theme (including custom color shades for the Bootstrap list groups), and a small number of variables such as $body-bg and the $font-family-base used for Bootstrap Fonts of specific headings. 

You can simply modify the color theme, by changing the variable $main-color in _myVariables.scss and recompiling the source files. All the colors get automatically adjusted depending on the $main-color variable. But you could also override other theme colors, such as the $primary or $secondary theme, or add an additional color in the themes colors map. (please see: https://getbootstrap.com/docs/4.3/getting-started/theming/ for more information).

Similarly, you can modify the $font-family-base variable to modify the font-family, or use a Google font instead. (Please note that you have to modify also the $font-family-base-sans-serif variable in the _variables.scss which is imported in mp.scss).
If you need to adjust or add specific Boostrap styles, you can do so after importing “myVariables” directly in darkCustom.scss (but before importing bootstrap), by either writing normal css or Scss code syntax. Remember to re-compile your source files for the changes to take effect.

#### Specific Case: Cards Customization:
A specific customization case (useful for the customer) is the .gridLayoutForCard which allows you to customize the grid layout for the service cards that appear in the custom landing page, by using Sass mixins for the columns. The example illustrates the div with class: col-xl-4 col-md-6 col-sm-12 mb-3
If you wish to have another grid layout, you can modify the numbers in the mixins appropriately.
(For more information about the Grid layout of Bootstrap, please see: https://getbootstrap.com/docs/4.0/layout/grid/)

### Advanced Sass Variables Template: _myVariables.scss

An extended variables file that serves as an example of custom Bootstrap UI components is here available for download:
[_myVariables.scss](advanced/_myvariables.scss)
_myVariablesExtended.scss
Simply replace your _myVariables.scss located in the "scss" folder with _myVariables.scss  that you have downloaded with the above link.

The variables are structured per UI component and the explanation is below:

#### Global options.
This section defines the enablement or disablement of general options like shadows, gradients, or border-radius for UI components such as buttons and list groups.


#### Fonts
The template includes a commented-out option if you wish to use a Google Font. $font-family-base is the variable used for fonts, and $font-size-base defines a variable for font sizes.

#### Navbar 
You can modify the padding of navbars.

#### Dropdowns 
Variables that affect Dropdown menus, such as the Dropdown appearing in the Navigation bar, or in the “Sorting” Dropdown of  the pagination.
For example, you can override the link-hover color or background hover color of a Dropdown, increase the border radius or increase the spaces between items.

#### Breadcrumbs
Similarly, this section defines parameters that affect the marketplace breadcrumb.

In darkCustom.scss there is also a section with styles about the breadcrumb. The reason for this is that mp.css also includes some styles for breadcrumbs and you can override these with the !important rule.

Similarly, the following sections about “Cards”, “List Groups”, “Buttons”, “Image Thumbnails”, “Popovers” and “Inputs” showcase some configurable variables for all these Bootstrap UI components.


### File mp.scss for adjusting theming of mp.css.
A minimum set of variables for the easier customization of mp.css has also been included via the Sass Variables importing mechanism. The variables that can be customized are: 1) colors such as $primary color, $warning, etc. 2) the $font-family-sans-serif and 3)the font sizes and line-heights of headings. 
In future updates, there might be additional variables that are customizeable.
