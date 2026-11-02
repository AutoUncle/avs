require "json"
require "json_schemer"
require_relative "avs/version"

# AVS – AutoUncle Vehicle Schema.
#
#   AVS.valid?(doc)      # => true / false
#   AVS.validate(doc)    # => [] or [#<AVS::Error pointer="/vehicle/vin" ...>]
#   AVS.validate!(doc)   # raises AVS::InvalidDocument
#
# A document may be a JSON string or a Hash with string or symbol keys.
module AVS
  SCHEMA_PATH = File.expand_path("avs/avs.schema.json", __dir__)

  Error = Struct.new(:pointer, :message, keyword_init: true) do
    def to_s = "#{pointer.empty? ? "/" : pointer}: #{message}"
  end

  class InvalidDocument < StandardError
    attr_reader :errors

    def initialize(errors)
      @errors = errors
      super("Invalid AVS document:\n#{errors.map { "  #{_1}" }.join("\n")}")
    end
  end

  class << self
    def schema = @schema ||= JSON.parse(File.read(SCHEMA_PATH)).freeze

    # The schema's own version, e.g. "0.1" (what documents put in avs.version).
    def schema_version = schema.dig("$defs", "Envelope", "properties", "version", "const")

    def validate(document)
      schemer.validate(normalize(document)).map do |error|
        Error.new(pointer: error["data_pointer"], message: error["error"])
      end
    end

    # True if the document is JSON and a valid AVS document; never raises.
    def valid?(document)
      validate(document).empty?
    rescue JSON::ParserError
      false
    end

    def validate!(document)
      errors = validate(document)
      raise InvalidDocument, errors if errors.any?

      true
    end

    private

    def schemer = @schemer ||= JSONSchemer.schema(schema, format: true)

    # Round-trips through JSON so symbol keys, Time and the like become plain JSON.
    def normalize(document)
      document.is_a?(String) ? JSON.parse(document) : JSON.parse(JSON.generate(document))
    end
  end
end
