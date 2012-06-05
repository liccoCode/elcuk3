$ ->
  $('a[rel=tooltip]').tooltip()

  $('input[data-provide=typeahead]').change ->
    target = $('#prod_list')
    sku = $(@).val()
    return false if sku.trim().length is 0
    target.mask("搜索中...")
    target.load('/products/p_search', sku: sku,
      ->
        target.unmask()
    )
