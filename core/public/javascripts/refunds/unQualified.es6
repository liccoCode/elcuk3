$(() => {

  $("#data_table").on("click", "input[name='refundBtn'],input[name='transferBtn']", function(e) {
    e.preventDefault();
    let memo = $(this).parent("td").parent("tr").find("input[name='memo']").val();
    let qty = $(this).parent("td").parent("tr").find("input[name='qty']").val();
    if (memo && qty) {
      $("#id_input").val($(this).data("id"));
      $("#qty_input").val(qty);
      $("#memo_input").val(memo);
      $("#data_form").attr("action", $(this).data("url")).submit();

    } else {
      noty({
        text: '必须填写处理说明和数量!',
        type: 'warning'
      });
    }

  });

});