# Scagen 

Scagen, pronounced [**ska**-gin], is a flexible site generating tool, which
allows for multiple markup dialects to convert easily to html.

## Goals
  
- It is useful to me
- The app is flexible
- The library is extensible

## App

Kick it off rather easily with n8han's [conscript] tool. Follow his steps for
installation.

Once you have conscript, installing scagen is as simple as

  cs philcali/scagen

### Usage

All inputs are defaults values. I've copied the usage straight from the source: 

    Usage: scagen [-h] [-r] [-i base-dir] [-o output-dir] [-t template] [-s stylesheet]
    -h help:       Prints this help
    -r recursive:  Crawls recursively, and copies to output, mirroring input (defaults false)
    -i base-dir:   Base directory to begin conversion (defaults .)
    -o output-dir: Output the conversion here (defaults converted)
    -t template:   Path to base template (defaults to base.ssp, which is included)
    -s stylesheet: Path to stylesheet (defaults to main.css, which is included)

So, an example would be

    scagen -r -i ~/working -o ~/Dropbox/Public

With this definition, scagen would recursively looks in `working`, copying everything it finds into
`~/Dropbox/Public` converting `.md`s, `.html`s, `.txt`s, or `.textile`s it finds using a `base.tpl`
you have in your working, or the one the library provides. Obviously, providing a path to the
template and style sheet will give you more customization.

## For Developers

Crawl anything and everything.... Talk about this more as the library becomes
more refined with use.
