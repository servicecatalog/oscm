**Customizing Cards**

A specific and quite useful customization option is `.gridLayoutForCard`. It determines the grid layout for the service cards on the marketplace using Sass mixins for the columns. The example illustrates the div with class: `col-xl-4 col-md-6 col-sm-12 mb-3`

To obtain a different grid layout, modify the numbers in the mixins accordingly. For more information about the grid layout of Bootstrap, refer to [https://getbootstrap.com/docs/4.0/layout/grid/](https://getbootstrap.com/docs/4.0/layout/grid/).

### Advanced Sass Variables Template: \_myvariables.scss

An extended version of the `_myvariables.scss` file, that serves as an example of custom Bootstrap UI components, is available for download [here](_myvariables.scss).

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
