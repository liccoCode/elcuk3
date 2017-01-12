$(() => {

  $("input[name='record.qty']").change(function() {
    if ($(this).val() + $(this).data("max") < 0) {
      noty({
        text: "调整的库存超过原始库存值！",
        type: 'warning'
      });
      $(this).val(0);
    }
  });

});