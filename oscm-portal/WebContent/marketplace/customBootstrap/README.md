# Marketplace Customization Guide with Bootstrap and Sass

## Introduction 

The current marketplace UI of OSCM is based on the [Bootstrap](https://getbootstrap.com/docs/4.3/getting-started/introduction/) framework. The default stylesheet, `mp.css`, is used for the customization of the marketplace layout and branding. Additional template stylesheets are provided in the `customBootstrap` folder of the OSCM branding package:

-   `customTheme.min.css`: a custom Bootstrap stylesheet (a fully compiled and customized version) with a dark navigation bar.

-   `customFooter.min.css`: a stylesheet to obtain a custom dark-background footer.

The pages of the marketplace UI use the `mp.min.css`, `customTheme.min.css`, and `customFooter.min.css` stylesheets. 

The following sections describe how to proceed to customize the marketplace UI based on Bootstrap, Sass (Syntactically Awesome Stylesheets), and the OSCM branding package.

For more information about the theming mechanism of Bootstrap via Sass in general, refer to [https://getbootstrap.com/docs/4.3/getting-started/theming/](https://getbootstrap.com/docs/4.3/getting-started/theming/). Another useful guide about how to customize Bootstrap is [http://bootstrap.themes.guide/how-to-customize-bootstrap.html](http://bootstrap.themes.guide/how-to-customize-bootstrap.html).


## Prerequisites:

The following prerequisites need to be fulfilled for customizing the marketplace UI:

-   Download the branding package provided with OSCM.

	The branding package can be downloaded as a ZIP file by the marketplace owner in the administration portal on the **Customize layout** page using the **Download branding package** option.

	The branding package includes the following folders containing Sass source files for customization:

	- `scss`: Contains the source files of mp.css

	-   `customBootstrap/scss`: Contains the theme source files for the custom Bootstrap stylesheets, explained in detail below.
	
	-   `font-awesome`: Contains a local copy of font-awesome with WebFonts and CSS. It is useful only if you wish to customize the font-awesome icons yourself. For more details look this [here](https://fontawesome.com/how-to-use/on-the-web/setup/hosting-font-awesome-yourself).

	The branding package also includes the required Bootstrap files, v4.3.1, in the `bootstrap` folder.

- Make sure that the import statements in the `.scss` files in the `scss` and `customBootstrap/scss` folders of the branding package have the correct relative paths to the Bootstrap source files in the `bootstrap` folder.

  Example:

  `@import "../../bootstrap/scss/variables";`

- Install and configure a Sass Compiler (CSS pre-processor).

  A Sass Compiler lets you compile your Sass source files (extension `.scss`) and generate CSS stylesheets. A simple option is to use Ruby. For more information on this option, refer to this [README](https://github.com/servicecatalog/oscm/tree/master/oscm-portal/WebContent/marketplace/scss/README.md).

  A good tool for re-compiling the branding package with a simple GUI is the [Koala APP](http://koala-app.com/). For more details on how to re-compile the branding package with Koala refer to [KoalaGuide.md](advanced/KoalaGuide.md).

 Another option is to use a tool available in the Internet to generate a Bootstrap template. Several tools are available that allow for an easy and visual customization of Bootstrap UI components, for example:
	
-  [Themestr.app](https://themestr.app/) themer or customizer.

- [Bootstrap Builder:](https://bootstrap.build/) Pre-defined Bootstrap themes, that are free to use and licensed under the MIT license, can be found [here](https://bootswatch.com/). Note, however, that these themes will not work out-of-the-box for the OSCM marketplace UI and need to be adjusted.

## Customizing Bootstrap UI Components

The customization of the Bootstrap components of the marketplace UI is accomplished by means of Sass variables.

The Sass variables and their values are defined in the separate files in the OSCM branding package.

The `customBootstrap\scss` folder contains the following variable files for the stylesheets in the same folder:

-   `_myVariables.scss`: defines the variables for the `customTheme` and `customFooter` stylesheets.

-   `_customVariables.scss`: defines the variables for the `customTheme` and `customFooter` stylesheets.

The `scss` folder contains the following variable file for the `mp.css` stylesheet:

-   `_variables.scss`

The general customization procedure is the following:

1.  Modify the variables in the variable file as desired. Remove the `!default` tag, if it is present. The tag denotes default Bootstrap values.

2.  Re-compile the source (`.scss` file) of the corresponding stylesheet.

The following sections provide some details on customizations you can carry out in the individual `.scss` files.


### Basic Sass Variables Template: \_myvariables.scss

The [`_myvariables.scss`](scss/_myvariables.scss) file (split in [`_darkVariables.scss`](scss/_darkVariables.scss) and [`_fontsVariables.scss`](scss/_fontsVariables.scss) in the `customBootstrap\scss` folder contains a minimum set of variables used for the default dark Bootstrap theme for the marketplace UI. It includes a green color theme (with custom color shades for the Bootstrap list groups) and variables such as `$body-bg` and `$font-family-base` for Bootstrap fonts of specific headings.

You can simply modify the color theme by changing the `$main-color` variable in `_myVariables.scss` and recompiling the source files. All the colors are automatically adjusted depending on the `$main-color` variable.

You can also override other theme colors, such as the `$primary` or `$secondary` color, or add colors to the theme color map. Refer to [https://getbootstrap.com/docs/4.3/getting-started/theming/](https://getbootstrap.com/docs/4.3/getting-started/theming/) for more information.

Similarly, you can modify the `$font-family-base` variable to change the font-family or use a Google font. Be aware that you also need to modify the `$font-family-base-sans-serif` variable in the `_variables.scss` file, which is imported into the `mp.css` style sheet.

If you would like to adjust or add specific Boostrap styles, you can do so directly in the [`customTheme.scss`](scss/customTheme.scss) file after importing `_myVariables` but before importing `bootstrap`. Use the standard css or Scss syntax.

Remember to re-compile your source files for any changes to take effect.

**Customizing Cards**

A specific and quite useful customization option is `.gridLayoutForCard`. It determines the grid layout for the service cards on the marketplace using Sass mixins for the columns. The example illustrates the div with class: `col-xl-4 col-md-6 col-sm-12 mb-3`

To obtain a different grid layout, modify the numbers in the mixins accordingly. For more information about the grid layout of Bootstrap, refer to [https://getbootstrap.com/docs/4.0/layout/grid/](https://getbootstrap.com/docs/4.0/layout/grid/).

### Advanced Sass Variables Template: \_myvariables.scss

An extended version of the `_myvariables.scss` file, that serves as an example of custom Bootstrap UI components, is available for download [here](advanced/_myvariables.scss).

In order to use it, simply replace the `_myVariables.scss` file in the `customBootstrap\scss` folder with the downloaded file.

The variables are sorted by UI components and explained in the following sections.

**Global options**

This section enables or disables general features like shadows, gradients, or the border radius for UI components such as buttons and list groups.

**Fonts**

This section includes commented lines to import and use a Google Font. `$font-family-base` is the variable used for fonts, `$font-size-base` defines font sizes.

**Navbar**

In this section, you can modify the padding of navigation bars.

**Dropdowns**

This section defines variables that affect dropdown menus, such as the one in the navigation bar, or the "sorting" dropdown for pagination. You can, for example, override the link hover color or the background hover color of the dropdowns, increase the border radius, or enlarge the space between items.


**Breadcrumbs**

This section defines parameters that affect the marketplace breadcrumbs.

`customTheme.scss` also contains a section with styles for breadcrumbs. The reason is that the `mp.css` stylesheet also includes some styles for breadcrumbs, which you can override with the `!important` rule.

**More Components**

The subsequent sections in the file show configurable variables for further Bootstratp UI components: Cards, List Groups, Buttons, Image Thumbnails, Popovers, and Inputs.

### Adjusting the Theming of mp.css: \_variables.scss

The [`_variables.scss`](../scss/_variables.scss) file in the `scss` folder contains a minimum set of variables to facilitate the customization of `mp.css`. The variables that can be adjusted include:

- Colors such as the primary or warning color.
- The font family, `$font-family-sans-serif`.
- Font sizes and line heights for headings.

Future updates may add more variables for customization.

## Deploying the Customized Stylesheets

After you have completed your customizations and recompiled the stylesheets, proceed as follows to get them deployed and effective:

1. Create an archive (ZIP) containing your customized files.

   The customized branding must be in the same structure as the branding package you downloaded in the OSCM administration portal.

2. Provide this archive to the platform operator so that he can deploy your customized branding package to the relevant container.

3. Ask the platform operator for the URL of the customized style sheet.

4. In the OSCM administration portal, go to the **Customize layout** page.

5. Select the marketplace for which you want to customize the layout. In the **Style sheet URL** field, enter the URL provided by the platform operator pointing to your customized style sheet.

   By default, the URL is as follows:

   `https://<host_fqdn>:8443/<folder-name>/css/mp.css`

   `<host_fqdn>` is the fully qualified name or IP address of the host used to access the platform, `8443` is the port. `<folder-name>` is the name of the folder containing the customized files.

   The location of the `customTheme.css` and `customFooter.css` files is automatically derived from the location of `mp.css`.

9.  Click **Save** to make the new layout and branding of your marketplace available.

The new layout becomes effective for a user the next time he logs in to the marketplace. Anonymous users need to close and reopen their Web browser.

