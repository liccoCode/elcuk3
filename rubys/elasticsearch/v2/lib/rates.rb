module Rates
  def google_rate(n = 1, currency)
    resp = self.class.get("https://www.google.com/finance/converter?a=#{n}&from=#{currency}&to=USD")
    doc = Nokogiri::HTML(resp.body)
    doc.at_css('#currency_converter_result span').text.split(" ")[0].strip.to_f
  end

  def cny_to_usd(n = 1)
    @rate ||= google_rate(n, 'CNY')
  end

  def gbp_to_usd(n = 1)
    @rate ||= google_rate(n, 'GBP')
  end

  def eur_to_usd(n = 1)
    @rate ||= google_rate(n, 'EUR')
  end

  def hkd_to_usd(n = 1)
    @rate ||= google_rate(n, 'HKD')
  end

  def jpy_to_usd(n = 1)
    @rate ||= google_rate(n, 'JPY')
  end

  def routine_cost_in_usd(row)
    payment_state = row.delete(:state)
    payment_rate = row.delete(:rate)
    
    if row[:currency] == 'USD'
      row[:cost]
    else
      payment_rate = payment_rate > 1 ? (1 / payment_rate) : payment_rate 
      rate = (payment_state == 'PAID' ? payment_rate : send("#{row[:currency].downcase}_to_usd"))
      row[:cost] * rate  
    end
  end
end