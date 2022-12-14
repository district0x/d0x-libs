(ns cljs-ipfs-api.files
  (:require [cljs-ipfs-api.core :as ipfs-core :refer-macros [defsignatures]]
            [cljs-ipfs-api.utils :refer [wrap-callback api-call js->cljkk cljkk->js]]))

; Untested methods are commented out
; If you need them, send a pull request with a test & comment them in
(defsignatures
  [[add [data [options] [callback]]]
   ; [files.addReadableStream [data [options] [callback]]]
   ; [files.addPullStream [[options]]]
   [cat [ipfs-path [options] [callback]] fcat]
   ; [files.catReadableStream (ipfsPath [options])]
   ; [files.catPullStream [ipfsPath [options]]]
   [get [ipfsPath [options], [callback]] fget]
   ; [files.getReadableStream [ipfsPath [options]]]
   ; [files.getPullStream [ipfsPath [options]]]
   [ls [ipfsPath [callback]] fls]
   ; [files.cp [from-to [callback]]]
   ; [files.mkdir [path [options callback]]]
   ; [files.stat [path [options callback]]]
   ; [files.rm [path [options callback]]]
   ; [files.read [path [options callback]]]
   ; [files.write [path content [options callback]]]
   ; [files.mv [from-to [callback]]]
   ; [files.ls [[path options callback]]]
   ; [files.flush [[path callback]] fflush]
   ])
