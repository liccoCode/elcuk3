$ ->
  $('a[rel=tooltip]').tooltip()

  $('input[data-provide=typeahead]').change ->
    target = $('#prod_list')
    target.mask("搜索中...")
    target.load('/products/p_search', sku: $(@).val(),
      ->
        target.unmask()
    )
