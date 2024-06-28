yieldUnescaped '<!DOCTYPE html>'
html(lang: 'en', 'xmlns': 'http://www.w3.org/1999/xhtml') {
    head {
        meta('charset': 'UTF-8')
        title('Groovy error page')
    }
    body {
        h1("HTTP ${errorStatus} - ${errorPhrase}")
        p("${errorMessage}")
    }
}
