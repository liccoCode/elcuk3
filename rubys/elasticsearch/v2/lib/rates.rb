module Rates
  def google_rate(n = 1)
    resp = self.class.get("https://www.google.com/finance/converter?a=#{n}&from=CNY&to=USD")
    doc = Nokogiri::HTML(resp.body)
    doc.at_css('#currency_converter_result span').text.split(" ")[0].strip.to_f
  end

  def cny_to_usd(n = 1)
    @rate ||= google_rate(n)
  end

  def routine_cost_in_usd(row)
    payment_state = row.delete(:state)
    payment_rate = row.delete(:rate)
    if row[:currency] == 'CNY'
      rate = (payment_state == 'PAID' ? payment_rate : cny_to_usd)
      # DB 里面的数据需要调整
      row[:cost] * (rate > 1 ? (1 / rate) : rate)
    else
      row[:cost]
    end
  end
end