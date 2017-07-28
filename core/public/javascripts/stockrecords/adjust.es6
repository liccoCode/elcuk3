$(() => {

  $("input[name='record.qty']").keyup(function() {
    if (Number($(this).val()) + Number($(this).data("max")) < 0) {
      noty({
        text: "调整的库存超过原始库存值！",
        type: 'warning'
      });
      $(this).val(0);
    }
  });

});