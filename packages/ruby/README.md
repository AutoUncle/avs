# autouncle-avs (Ruby)

The [AVS](https://github.com/AutoUncle/avs) JSON Schema and a validator.

```ruby
gem "autouncle-avs", "~> 0.3", source: "https://rubygems.pkg.github.com/autouncle"
```

```ruby
require "avs"

AVS.valid?(doc)       # => true / false
AVS.validate(doc)     # => [] or [#<struct AVS::Error pointer="/vehicle/vin", message="...">]
AVS.validate!(doc)    # raises AVS::InvalidDocument listing every error
AVS.schema            # the schema as a frozen Hash
```

`doc` can be a JSON string or a Hash with string or symbol keys.

In a contract test:

```ruby
it "returns a valid AVS document" do
  get "/api/v1/vehicles/#{uuid}.avs"
  expect(AVS.validate(response.parsed_body)).to be_empty
end
```

The gem version equals the schema release (`0.3.x` bundles schema `0.3`).
