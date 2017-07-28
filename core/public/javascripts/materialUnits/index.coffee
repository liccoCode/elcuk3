$ ->



  $("#create_deliverplan_btn").click (e) ->
    e.preventDefault()
    $form = $("#create_materialUnit")
    window.open('/MaterialPlans/materialPlan?' + $form.serialize(), "_blank")


