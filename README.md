# district-parsers

[![Build Status](https://travis-ci.org/district0x/district-parsers.svg?branch=master)](https://travis-ci.org/district0x/district-parsers)


Set of functions helpful for **safe** parsing. Safe parsing functions always return desired type or nil, or default value.
They never throw error for any kind of input. 


## Installation
Add `[district0x/district-parsers "1.0.0"]` into your project.clj  
Include `[district.parsers]` in your CLJS file  

## API Overview
- [district.parsers](#districtparsers)
  - [parse-int](#parse-int)
  - [parse-float](#parse-float)
  - [parse-keyword](#parse-keyword)
  - [parse-boolean](#parse-boolean)
  - [parse-non-empty-str](#parse-non-empty-str)
  - [parse-web3-address](#parse-web3-address)
  - [parse-int-seq](#parse-int-seq)
  - [parse-float-seq](#parse-float-seq-seq)
  - [parse-keyword-seq](#parse-keyword-seq)
  - [parse-boolean-seq](#parse-boolean-seq)
  - [parse-non-empty-str-seq](#parse-non-empty-str-seq)
  - [parse-web3-address-seq](#parse-web3-address-seq)
  

## district.parsers
#### <a name="parse-int">`parse-int [x & [default]]`
Parses int. If unparsable, returns nil or default. 
```clojure
(parsers/parse-int "1")
;; => 1
(parsers/parse-int "abc")
;; => nil
(parsers/parse-int "a" 1)
;; => 1
(parsers/parse-int [1])
;; => nil
```


#### <a name="parse-float">`parse-float [x & [default]]`
Parses float. If unparsable, returns nil or default. 
```clojure
(parsers/parse-float "1.1")
;; => 1.1
(parsers/parse-float "1,1")
;; => 1.1
(parsers/parse-float "a" 1)
;; => 1
```

#### <a name="parse-keyword">`parse-keyword [x & [default]]`
Parses keyword. If unparsable, returns nil or default.
```clojure
(parsers/parse-keyword ":a")
;; => :a
(parsers/parse-keyword ":a/b")
;; => :a/b
(parsers/parse-keyword "a")
;; => :a
(parsers/parse-keyword "a/b")
;; => :a/b
```


#### <a name="parse-boolean">`parse-boolean [x & [default]]`
Parses boolean string. If unparsable, returns nil or default.
```clojure
(parsers/parse-boolean "true")
;; => true
(parsers/parse-boolean "FALSE")
;; => false
(parsers/parse-boolean "a")
;; => nil
```


#### <a name="parse-non-empty-str">`parse-non-empty-str [x & [default]]`
Parses non empty string. If unparsable or empty string given, returns nil or default.  
```clojure
(parsers/parse-non-empty-str "")
;; => nil
(parsers/parse-non-empty-str [1])
;; => nil
(parsers/parse-non-empty-str "a")
;; => "a"
(parsers/parse-non-empty-str 1)
;; => "1"
```


#### <a name="parse-web3-address">`parse-web3-address [x & [default]]`
Parses web3.js address and transforms it to lowercase. If invalid address is given, returns nil or default.  
```clojure
(parsers/parse-web3-address "0x0B73EEc3b1C5A2c555799B7FfC500082606DFf15")
;; => "0x0b73eec3b1c5a2c555799b7ffc500082606dff15"
(parsers/parse-web3-address "0x0")
;; => nil
```


#### <a name="parse-int-seq">`parse-int-seq [coll & [default]]`
Parses sequence of ints. Unparsable ones will be replaced by nil or default. Ensures sequence if not passed. 
```clojure
(parsers/parse-int-seq ["1,1" "a"] 10)
;; => [1 10]
(parsers/parse-int-seq "1")
;; => [1]
```


#### <a name="parse-float-seq">`parse-float-seq [coll & [default]]`
Parses sequence of floats. Unparsable ones will be replaced by nil or default. Ensures sequence if not passed. 
```clojure
(parsers/parse-float-seq ["1.1" "1,1"])
;; => [1.1 1.1]
```

#### <a name="parse-keyword-seq">`parse-keyword-seq [coll & [default]]`
Parses sequence of keywords. Unparsable ones will be replaced by nil or default. Ensures sequence if not passed. 
```clojure
(parsers/parse-keyword-seq ["a" ":a"])
;; => [:a :a]
```

#### <a name="parse-boolean-seq">`parse-boolean-seq [coll & [default]]`
Parses sequence of booleans. Unparsable ones will be replaced by nil or default. Ensures sequence if not passed. 
```clojure
(parsers/parse-boolean-seq ["true" "false"])
;; => [true false]
```

#### <a name="parse-non-empty-str-seq">`parse-non-empty-str-seq [coll & [default]]`
Parses sequence of non empty string. Unparsable ones will be replaced by nil or default. Ensures sequence if not passed. 
```clojure
(parsers/parse-non-empty-str-seq ["" "a"])
;; => [nil "a"]
```

#### <a name="parse-web3-address-seq">`parse-web3-address-seq [coll & [default]]`
Parses sequence of web3-addresss. Unparsable ones will be replaced by nil or default. Ensures sequence if not passed. 
```clojure
(parsers/parse-web3-address-seq ["0x0B73EEc3b1C5A2c555799B7FfC500082606DFf15" "a"] "0x0")
;; => ["0x0b73eec3b1c5a2c555799b7ffc500082606dff15" "0x0"]
```

#### <a name="parse-seq-fn">`parse-seq-fn [parse-fn]`
Creates a parsing function for sequences.  
```clojure
(parse-seq-fn parsers/parse-int)
;; is the same as 
parsers/parse-int-seq
```

## Development
```bash
lein deps
# To run tests and rerun on changes
lein doo chrome tests
```