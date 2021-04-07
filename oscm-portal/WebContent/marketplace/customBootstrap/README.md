# Marketplace Customization Guide with Bootstrap and Sass

## Introduction 

The current marketplace UI of OSCM is based on the [Bootstrap](https://getbootstrap.com/docs/4.3/getting-started/introduction/) framework. The default stylesheet, `mp.css`, is used for the customization of the marketplace layout and branding. Additional template stylesheets are provided in the `customBootstrap` folder of the OSCM branding package:

-   `customTheme.min.css`: a custom Bootstrap stylesheet (a fully compiled and customized version) with a dark navigation bar.

-   `customFooter.min.css`: a stylesheet to obtain a custom dark-background footer.

The pages of the marketplace UI use the `mp.min.css`, `customTheme.min.css`, and `customFooter.min.css` stylesheets. 

The following sections describe how to proceed to customize the marketplace UI based on Bootstrap, Sass (Syntactically Awesome Stylesheets), and the OSCM branding package.

For more information about the theming mechanism of Bootstrap via Sass in general, refer to [https://getbootstrap.com/docs/4.3/getting-started/theming/](https://getbootstrap.com/docs/4.3/getting-started/theming/). Another useful guide about how to customize Bootstrap is [http://bootstrap.themes.guide/how-to-customize-bootstrap.html](http://bootstrap.themes.guide/how-to-customize-bootstrap.html).


## Prerequisites

The following prerequisites need to be fulfilled for customizing the marketplace UI:

-   Download the branding package provided with OSCM.

	The branding package can be downloaded as a ZIP file by the marketplace owner in the administration portal on the **Customize layout** page using the **Download branding package** option.

	The branding package includes the following folders containing Sass source files for customization:

	- `scss`: Contains the source files of mp.css

	-   `customBootstrap/scss`: Contains the theme source files for the custom Bootstrap stylesheets, explained in detail below.
	
	-   `font-awesome`: Contains a local copy of font-awesome with WebFonts and CSS. It is useful only if you wish to customize the font-awesome icons yourself. For more details look this [here](https://fontawesome.com/how-to-use/on-the-web/setup/hosting-font-awesome-yourself).

	The branding package also includes the required Bootstrap files, v4.3.1, in the `bootstrap` folder.

- Make sure that the import statements in the `.scss` files in the `scss` and `customBootstrap/scss` folders of the branding package have the correct relative paths to the Bootstrap source files in the `bootstrap` folder.

  Example: `@import "../../bootstrap/scss/variables";`

-   Install and configure a Sass Compiler (CSS pre-processor).

    > **_Note_**: This step is optional as the source files are re-compiled when the oscm-branding container is re-deployed.
  

	A Sass Compiler lets you compile your Sass source files (extension `.scss`) and generate CSS stylesheets. A simple option is to use Ruby. For more information on this option, refer to this [README](https://github.com/servicecatalog/oscm/tree/master/oscm-portal/WebContent/marketplace/scss/README.md).

     A good tool for re-compiling the branding package with a simple GUI is the [Koala APP](http://koala-app.com/). For more details on how to re-compile the branding package with Koala refer to [KoalaGuide.md](advanced/KoalaGuide.md).

    Another option is to use a tool available in the Internet to generate a Bootstrap template. Several tools are available that allow an easy and visual customization of Bootstrap UI components, for example:
	
      -  [Themestr.app](https://themestr.app/) themer or customizer.

      - [Bootstrap Builder:](https://bootstrap.build/) Pre-defined Bootstrap themes, that are free to use and licensed under the MIT license, can be found [here](https://bootswatch.com/). Note, however, that these themes will not work out-of-the-box for the OSCM marketplace UI and need to be adjusted.

- Lastly, OSCM provides the option of the **Runtime theme customization**. This option works either in the usual way with a) pre-compilation of source files or b) at Runtime in Marketplace UI without the need of a Sass pre-compiler. The changes take effect immediately upon "Save" of the Profile page. Please refer to the respective [section](#customizing-the-marketplace-ui-at-runtime) for more details.

## Customizing Bootstrap UI Components

The customization of the Bootstrap components of the marketplace UI is accomplished by means of Sass variables.

The Sass variables and their values are defined in the separate files in the OSCM branding package.

The `customBootstrap\scss` folder contains the following variable files for the stylesheets in the same folder:

-   `_myVariables.scss`: defines the variables for the `customTheme` and `customFooter` stylesheets.

-   `_customVariables.scss`: defines the variables for the `customTheme` and `customFooter` stylesheets.
-    `.\basic\_colors.scss`: defines the theme colors as custom CSS variables. Refer to the section about [theme variables modification](#modification-of-theme-variables-in-the-source-files) for more details.

The `scss` folder contains the following variable file for the `mp.css` stylesheet:

-   `_variables.scss`

The general customization procedure is the following:

1.  Modify the variables in the variable file as desired. Remove the `!default` tag, if it is present. The tag denotes default Bootstrap values.

2.  Re-compile the source (`.scss` file) of the corresponding stylesheet.

The following sections provide some details on customizations you can carry out in the individual `.scss` files.


### Basic Sass Variables Template: \_myvariables.scss

The [`_myvariables.scss`](scss/_myvariables.scss) file and [`_fontsVariables.scss`](scss/_fontsVariables.scss) in the `customBootstrap\scss` folder contains a minimum set of variables used for the default Bootstrap theme for the marketplace UI (which includes a light theme with a dark navigation bar and footer). It includes a green color theme (with custom color shades for the Bootstrap list groups) and variables such as `$bg-body` and `$font-family-base` for Bootstrap fonts of specific headings.

You can modify the color theme variables as CSS custom properties in `_colors.scss` as described in [section](#modification-of-theme-variables-in-the-source-files) and recompiling the source files. For example, you can modify the CSS variable `--oscm-primary`. All the colors are automatically adjusted depending on the defined variables.

You can also override Sass variables for the colors of specific components in `_customVariables.scss`, such as `$breadcrumb-bg`, `$dropdown-bg`, etc. Refer to [https://getbootstrap.com/docs/4.3/getting-started/theming/](https://getbootstrap.com/docs/4.3/getting-started/theming/) for more information on Bootstrap theming in general.

Similarly, you can modify the `$font-family-base` variable to change the font-family or use a Google font. Be aware that you also need to modify the `$font-family-base-sans-serif` variable in the `_variables.scss` file, which is imported into the `mp.css` style sheet.

If you would like to adjust or add specific Boostrap styles, you can do so directly in the [`customTheme.scss`](scss/customTheme.scss) file after importing `_myVariables` but before importing `bootstrap`. Use the standard css or Scss syntax.

Remember to re-compile your source files for any changes to take effect.

### Advanced Variables Template: \_myvariables.scss

You can find [here](advanced/advanced.md) a guide to advanced customization with an extended `_myvariables.scss` template.

### Adjusting the Theming of mp.css: \_variables.scss

The [`_variables.scss`](../scss/_variables.scss) file in the `scss` folder contains a minimum set of variables to facilitate the customization of `mp.css`. The variables that can be adjusted include:

- Colors such as the warning color.
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

## Customizing the Marketplace UI at Runtime

In the Marketplace UI at the `Account / Profile` page you can modify a set of variables which allow you the dynamic theming of your marketplace. All variables are listed in the `Choose your color scheme` section.

These variables are: 
1. `Dark / Light`: Choose between a dark background or a light background theme.
2. `Primary color`: this is the color of buttons, list-group-items, links and other items. Used to provide an "accent" color to your UI.
3. `Foreground text`: the color of the foreground text (such as text appearing in the different sections, in the account menu, etc).
4. `Navbar background color`: the background color of the navigation bars for the header, footer and the account menu.
5. `Navbar link color`: the color of links which appear in the navigation bars for the header, footer and the account menu.
6. `Input field background color`: the background color of form inputs.

### Modification of theme variables in the source files

You can pre-compile the `.scss` source files and re-deploy them in the usual way. 

The variables (defined as CSS custom properties) for the dynamic theming are located in `customBootstrap\scss\basic\_colors.scss`. 

The variables for the light theme are located under the `:root` element and those for the dark theme 
under `:root[data-theme="dark"]`. The most important of them (defined as hsl values) are the following:

- `--oscm-main`: the "main" background color. The background color of the breadcrumbs, sections such as the different page panels, and the items in the account menu depend on `--oscm-main`. To change its value, simply modify the values 
`--oscm-main-h` for the hue value, `--oscm-main-s` for saturation and `--oscm-main-l` for lightness.
- `--oscm-bg-color`: the theme background color. It adjusts to `--oscm-main`. For dark themes, it is a dark color variant, where as for light themes, it is a light version of `--oscm-main`.
- `--oscm-primary`: the theme primary color. To change it, simply modify the values: `--oscm-primary-h`, `--oscm-primary-s` and `--oscm-primary-l`.
- `--oscm-main-font-color`: the foreground text color of the theme. To change it, modify the h, s and l values.
- `--oscm-main-100`, `--oscm-main-200`, `--oscm-main-400`, `--oscm-main-800`, `--oscm-main-900`: shades of  `--oscm-main` with different degrees of lightness, ordered from the lightest to the darkest. To create darker or lighter shades of `--oscm-main`,  simply modify the last percentage parameter, which is the lightness of the color.
- `--oscm-main-input-color`: the background color of input fields.
- `--oscm-navbar-color`: the navbar background color (for the header, the footer and the account menu).
- `--oscm-navbar-links-color`: the navbar link color (for the header, the footer and the account menu).
