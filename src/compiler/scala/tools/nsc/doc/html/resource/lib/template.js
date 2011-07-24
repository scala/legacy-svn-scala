/* Copyright 2005-2011 LAMP/EPFL
 * Code by Gilles Dubochet with contributions by Pedro Furlanetto, Ruediger Keller
 */

$(document).ready(function() {
  
  // Member opening/closing support
  var closedHeight = 17;
  $("#template .fullcomment").each(function() {
    var commentElement = $(this);
    var openHeight = commentElement.height();
    if(openHeight > 27) {
      this.parentNode.openHeight = openHeight;
      commentElement.addClass("openable");
      commentElement.height(closedHeight);
    }
  });
  
  // Add [use case] labels
  $("#template .fullcomment.useCase .comment").each(function() {
    var commentElement = $(this);
    var firstChild = commentElement.children().first();
    if(firstChild.is("p"))
      firstChild.prepend("[Use Case] ")
    else
      commentElement.prepend("<p>[Use Case]</p>")
  });

  // Member filter box
  var input = $("#textfilter input");
  input.bind("keyup", function(event) {
    if (event.keyCode == 27)
      input.val(""); // escape key
    filter(true);
  });
  input.focus(function(event) {
    input.select();
  });
  $("#textfilter > .post").click(function() {
    $("#textfilter input").attr("value", "");
    filter();
  });
  $(document).keydown(function() {
    if (document.activeElement != $("#textfilter input")[0])
      $("#textfilter input").focus();
  });
  $("#textfilter input").focus();

  // Various initializations
  initializePage();

  // Create tooltips
  $(".extype, .defval").each(function() {
    var me = $(this);
    me.attr("title", me.attr("name"));
  });

  // Add toggle arrows to members
  var docAllSigs = $("#template li").has(".openable").find(".signature");
  docAllSigs.addClass("closed").click(function() {
    var signature = $(this);
    var fullComment = signature.parent().find(".fullcomment");
    if (fullComment.height() == closedHeight)
      fullComment.animate({ 'height' : this.parentNode.openHeight + 'px' }, 100);
    else
      fullComment.animate({ 'height' : closedHeight + 'px' }, 100);
    signature.toggleClass("closed");
    signature.toggleClass("opened");
  });

  // Toggle for linear super types and known subclasses
  $(".toggleContainer").click(function() {
    var container = $(this);
    container.toggleClass("open");
    var content = $(".hiddenContent", container);
    content.slideToggle(100);
  });

  // Set parent window title
  windowTitle();

  // Initialize Buttons 
  initButtons();
  
  // Ancestors on filter bar
  function isVisibleByDefaultAncestor() {
    var name = $(this).attr("name");
    return (name != 'scala.Any' && name != 'scala.AnyRef') || name == document.title;
  }
  var ancestors = $("#ancestors .toggleButton");
  var visibleByDefaultAncestors = ancestors.filter(isVisibleByDefaultAncestor);
  ancestors.click(filter);
  visibleByDefaultAncestors.addClass("in");
  
  // Buttons on filter bar
  $("#order, #visibility").click(function() {
    adjustTextfilterWidth();
    filter(true);
  });
  $("#inheritance").click(function() {
    adjustTextfilterWidth();
    if($("#inheritance .show").is(":visible"))
      visibleByDefaultAncestors.addClass("in");
    else {
      ancestors.removeClass("in").first().addClass("in");
    }
    filter(true);
  });
  $("#linearization").click(function() {
    updateAncestorsVisibility();
    adjustTextfilterWidth();
  });
  
  // Pre-filter members
  filter();
  
  // Adjust member filter box width to windows size
  adjustTextfilterWidth();
  $(window).resize(adjustTextfilterWidth);
});

function adjustTextfilterWidth() {
  var textfilter = $("#textfilter");
  var sibling = textfilter.prev();
  var left = sibling.offset().left + sibling.outerWidth();
  var innerWidth = window.innerWidth ? (window.innerWidth - 26) : (document.documentElement.clientWidth - 10);
  var width = innerWidth - left;
  textfilter.width(width);
  
  $("#definition").css("margin-top", $("#mbrsel").outerHeight());
}

function initButtons() {
  $(".button > div").hide();
  $(".button > div:first-child").show();

  var blurFunc = function() {
    $(this).removeClass("in");
  };
  
  $(".button").mousedown(function(event) {
    $(this).addClass("in");
    event.preventDefault(); // Prevent text selection on click
  }).mouseup(blurFunc).mouseout(blurFunc).click(function() {
    var spans = $(this).find("> div");
    var curr = spans.filter(":visible");
    var next = curr.next();
    if(!next.length) next = spans.first();
    if(curr.length && next.length && curr[0] != next[0]) {
      curr.hide();
      next.show();
    }
  });
  
  $(".toggleButton").mousedown(function(event) {
    event.preventDefault(); // Prevent text selection on click
  }).click(function() {
    $(this).toggleClass("in");
  });
}

function updateAncestorsVisibility() {
  if($("#linearization .hide").is(":visible"))
    $("#ancestors").show();
  else
    $("#ancestors").hide();
}

/**
 * Initializes the page:
 * - Hide all statically-generated parents headings.
 * - Copy all members from the value and type members lists (flat members) to
 *   corresponding lists nested below the parent headings (inheritance-grouped members).
 * - Initialises a control variable used by the filter method to control whether
 *   filtering happens on flat members or on inheritance-grouped members.
 * - Mark elements not in a parent/inherit section for faster filtering.
 * - Determine the the comment text for each member for faster filtering.
 */
function initializePage() {
  // Parents is a map from fully-qualified names to the DOM node of parent headings.
  var parents = new Object();
  $("#inheritedMembers > .parent").each(function() {
    parents[$(this).attr("name")] = $(this);
  });
  
  function copyMembers(element, selector, structure) {
    var mbr = $(element);
    this.mbrText = mbr.find("> .fullcomment .cmt").text();
    var qualName = mbr.attr("name");
    var owner = qualName.slice(0, qualName.indexOf("#"));
    var name = qualName.slice(qualName.indexOf("#") + 1);
    var parent = parents[owner];
    if (parent != undefined) {
      var members = $(selector, parent);
      if (members.length == 0) {
        parent.append(structure);
        members = $(selector, parent);
      }
      var clone = mbr.clone();
      clone[0].mbrText = element.mbrText;
      clone[0].openHeight = element.openHeight;
      members.append(clone);
    }
  }
  $("#types > ol > li").each(function() {
    copyMembers(this, "> .types > ol", "<div class='types members'><h3>Type Members</h3><ol></ol></div>");
  });
  $("#values > ol > li").each(function() {
    copyMembers(this, "> .values > ol", "<div class='values members'><h3>Value Members</h3><ol></ol></div>");
  });

  $("#inheritedMembers > div.parent").each(function() {
    if ($("> div.members", this).length == 0) {
      $(this).remove();
    }
    ;
  });
};

function filter(scrollToMember) {
  var query = $.trim($("#textfilter input").val()).toLowerCase();
  query = query.replace(/[-[\]{}()*+?.,\\^$|#]/g, "\\$&").replace(/\s+/g, "|");
  var queryRegExp = new RegExp(query, "i");
  var privateMembersHidden = $("#visibility .public").is(":visible");
  var orderingAlphabetic = $("#order .alpha").is(":visible");
  var hiddenSuperclassElements = orderingAlphabetic ? $("#ancestors > .toggleButton").not(".in") : $("#ancestors > .toggleButton:gt(0)");
  var hiddenSuperclasses = hiddenSuperclassElements.map(function() {
    return $(this).attr("name");
  }).get();

  var hideInheritedMembers;
  
  if(orderingAlphabetic) {
    $("#inheritedMembers").hide();
    hideInheritedMembers = true;
    $("#allMembers > .members").each(filterFunc);
  }
  else {
    $("#inheritedMembers").show();
    hideInheritedMembers = true;
    $("#allMembers > .members").each(filterFunc);
    hideInheritedMembers = false;
    $("#inheritedMembers > .parent > .members").each(filterFunc);
  }
  
  function filterFunc() {
    var membersVisible = false;
    var members = $(this);
    members.find("> ol > li").each(function() {
      var mbr = $(this);
      if (privateMembersHidden && mbr.attr("visbl") == "prt") {
        mbr.hide();
        return;
      }
      var name = mbr.attr("name");
      // Owner filtering must not happen in "inherited from" member lists
      if (hideInheritedMembers) {
        var ownerIndex = name.indexOf("#");
        if (ownerIndex < 0) {
          ownerIndex = name.lastIndexOf(".");
        }
        var owner = name.slice(0, ownerIndex);
        for (var i = 0; i < hiddenSuperclasses.length; i++) {
          if (hiddenSuperclasses[i] == owner) {
            mbr.hide();
            return;
          }
        }
      }
      if (query && !(queryRegExp.test(name) || queryRegExp.test(this.mbrText))) {
        mbr.hide();
        return;
      }
      mbr.show();
      membersVisible = true;
    });
    
    if (membersVisible)
      members.show();
    else
      members.hide();
  };

  if (scrollToMember) {
    var comment = $("#comment");
    window.scrollTo(0, comment.offset().top + comment.outerHeight(true) - $("#mbrsel").outerHeight());
  }

  return false;
};

function windowTitle() {
  try {
    parent.document.title = document.title;
  } catch (e) {
    // Chrome doesn't allow settings the parent's title when
    // used on the local file system.
  }
};
