# district-sendgrid

[![Build Status](https://travis-ci.org/district0x/district-sendgrid.svg?branch=master)](https://travis-ci.org/district0x/district-sendgrid)

Helper functions for sending emails via Sendgrid API.

# Installation

<!-- Add `[district0x/district-sendgrid "1.0.0"]` into your project.clj -->

This module is available as a Maven artifact from Clojars. The latest released version is:
[![Clojars Project](https://img.shields.io/clojars/v/district0x/district-sendgrid.svg)](https://clojars.org/district0x/district-sendgrid)
<br>

Include `[district.sendgrid]` in your CLJS file, where you use `mount/start`

# district.sendgrid

## `send-email [opts]`

Sends request to Sendgrid to construct and send email from pre-defined template

**opts:**
* `:from` From email address
* `:to` To email address
* `:subject` Email subject
* `:content` Main content of email
* `:substitutions` Map defining template substituions
* `:on-success` On success callback
* `:on-error` On error callback
* `:template-id` Sendgrid template ID
* `:api-key` Sendgrid API key
* `:print-mode?` If true, prints emails to console, instead of sending it
