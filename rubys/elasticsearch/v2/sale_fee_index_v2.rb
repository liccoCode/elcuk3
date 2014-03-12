require "./backend"

class SaleFeeActor
  include Celluloid
  include ActorBase

  def initialize
    init_attrs
    @http = Request.new
  end

  MAPPING = %q()


end