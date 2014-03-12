require "./backend"

class ProcurePayUnitActor
  include Celluloid
  include ActorBase

  def initialize
    init_attrs
    @es_index = "elcuk2"
    @es_type = "procurepayunit"
  end

  MAPPING = %q()
end