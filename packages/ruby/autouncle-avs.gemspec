require_relative "lib/avs/version"

Gem::Specification.new do |spec|
  spec.name = "autouncle-avs"
  spec.version = AVS::VERSION
  spec.authors = ["AutoUncle"]
  spec.summary = "AVS – AutoUncle Vehicle Schema: the JSON Schema and a validator"
  spec.description = "Bundles the AVS JSON Schema and validates AVS documents against it."
  spec.homepage = "https://github.com/AutoUncle/avs"
  spec.license = "Apache-2.0"
  spec.required_ruby_version = ">= 3.1"

  spec.metadata["source_code_uri"] = "https://github.com/AutoUncle/avs/tree/main/packages/ruby"
  spec.metadata["github_repo"] = "ssh://github.com/AutoUncle/avs"
  spec.metadata["rubygems_mfa_required"] = "true"

  spec.files = Dir["lib/**/*.{rb,json}", "README.md"]
  spec.require_paths = ["lib"]

  spec.add_dependency "json_schemer", "~> 2.3"
end
