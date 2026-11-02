require "minitest/autorun"
require "avs"

class AVSTest < Minitest::Test
  ROOT = File.expand_path("../../..", __dir__)
  EXAMPLES = Dir["#{ROOT}/examples/*.json"]
  INVALID = Dir["#{ROOT}/tests/invalid/*.json"]
  SCHEMA_VERSION = File.read("#{ROOT}/VERSION").strip.split(".").first(2).join(".")

  def example(name) = JSON.parse(File.read(EXAMPLES.find { _1.end_with?(name) }))

  def test_examples_are_valid
    refute_empty EXAMPLES
    EXAMPLES.each { |path| assert AVS.valid?(File.read(path)), "#{path}: #{AVS.validate(File.read(path)).join(', ')}" }
  end

  def test_invalid_fixtures_are_rejected
    refute_empty INVALID
    INVALID.each { |path| refute AVS.valid?(File.read(path)), "#{path} should be rejected" }
  end

  def test_accepts_symbol_keys
    assert AVS.valid?({ avs: { version: SCHEMA_VERSION, kind: "type" }, identity: { brand: { name: "BMW" }, model: { name: "3 Series" } } })
  end

  def test_reports_pointer_for_bad_vin
    doc = example("vehicle-bmw-320e-g20.json")
    doc["vehicle"]["vin"] = "NOT-A-VIN"
    errors = AVS.validate(doc)
    assert_equal ["/vehicle/vin"], errors.map(&:pointer)
  end

  def test_requires_identity
    errors = AVS.validate({ "avs" => { "version" => SCHEMA_VERSION, "kind" => "type" } })
    refute_empty errors
  end

  def test_validate_bang_raises_with_errors
    error = assert_raises(AVS::InvalidDocument) { AVS.validate!({}) }
    refute_empty error.errors
  end

  def test_valid_is_false_for_malformed_json
    refute AVS.valid?("{")
    assert_raises(JSON::ParserError) { AVS.validate("{") }
  end

  def test_schema_version
    assert_equal SCHEMA_VERSION, AVS.schema_version
  end
end
