$(() => {

  $("#pro_category").change(function () {
    $('#pro_sku').val($(this).val()).focus();
  });

  $("#pro_sku").keyup(function () {
    $(this).val($(this).val().toUpperCase());
  });

  $("#create_product_form").on("click", "#more_locate_btn, #more_selling_point_btn", function () {
    let div = $("#" + $(this).data("table"));
    let rowsCount = div.find(".form-group").length;
    let cloneDiv = div.find(".form-group")[rowsCount - 1];
    let newDiv = cloneDiv.cloneNode(true);
    let textareas = newDiv.getElementsByTagName("textarea");
    setTextAreaName($(this).attr("id"), rowsCount, textareas);
    $("#" + $(this).data("table")).append(newDiv);
  });

  function setTextAreaName (flag, rowsCount, textareas) {
    if (flag == "more_locate_btn") {
      textareas[0].name = "pro.locate[" + rowsCount + "].title";
      textareas[0].value = "";
      textareas[1].name = "pro.locate[" + rowsCount + "].content";
      textareas[1].value = "";
    } else {
      textareas[0].name = "pro.sellingPoint[" + rowsCount + "].title";
      textareas[0].value = "";
      textareas[1].name = "pro.sellingPoint[" + rowsCount + "].content";
      textareas[1].value = "";
    }
  }

  $(document).on("change", "#pro_weight_g, #pro_productWeight_g", function () {
    let $input = $(this);
    if ($input.val() !== "" && $input.val() > 0 && !isNaN($input.val())) {
      let select = "";
      if ($input.attr('id') === 'pro_weight_g') {
        select = "input[name='pro.weight']"
      } else {
        select = "input[name='pro.productWeight']"
      }
      $(select).val(($input.val() / 1000).toFixed(2))
      $(select).trigger('change')
    }
  }).on("change", "input[name='pro.weight'], input[name='pro.productWeight']", function () {
    //将输入的重量(kg)换算成盎司(oz)
    let $input = $(this);
    if ($input.val() !== "" && $input.val() > 0 && !isNaN($input.val())) {
      let $span = $("<span style='margin-left:10px;'>or: " + ($input.val() * 35.2739619).toFixed(2) + "</span>");
      $input.parent().next().empty();
      $input.parent().after($span);
    }
  }).on("change", "input[name='pro.lengths'], input[name='pro.width'], input[name='pro.heigh'], input[name='pro.productLengths'], input[name='pro.productWidth'], input[name='pro.productHeigh']", function () {
    //将输入的长度、宽度、高度换算成英寸(inch)
    let $input = $(this);
    if ($input.val() !== "" && $input.val() > 0 && !isNaN($input.val())) {
      let $span = $("<span style='margin-left:10px;'>inch: " + ($input.val() * 0.0393701).toFixed(2) + "</span>");
      $input.parent().next().empty();
      $input.parent().after($span);
    }
  });

  $("#create_product_form").on("click", "[name^='delete_locate_row'], [name^='delete_selling_point_row']", function () {
    let $btn = $(this);
    $btn.parent("div").parent().remove()
  });

});