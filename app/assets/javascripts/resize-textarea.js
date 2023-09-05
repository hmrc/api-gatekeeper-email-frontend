function auto_grow(element) {
  element.style.height = "auto";
  element.style.height = (element.scrollHeight) + "px";
}

function initAutoGrowOnInput(inputElementId) {
  if (inputElementId != null) {
    var inputElement = document.getElementById(inputElementId)
    if (inputElement != null) {
      inputElement.addEventListener(
          'input',
          function () {
            auto_grow(inputElement)
          },
          false
      )
    }
  }
}