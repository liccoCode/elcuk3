$ ->
  # 生成年度销售目标的 option 从今年 至 往后十年
  $targetDate = $('select[name="targetDate"]')
  year = new Date().getFullYear()
  range = [-4..5]
  for n in range
    $targetDate.append("<option value='#{year + n}'>#{year + n}</option>")
