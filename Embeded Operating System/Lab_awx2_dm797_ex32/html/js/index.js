$(function(){
	
	//configuration
	var width = 800;
	var animationSpeed = 1000;
	var pause = 3000;
	var currentSlide = 1;
	
	//cache DOM
	var $slider = $("#picSlider");
	var $slideContainer = $slider.find(".slides");
	var $slides = $slideContainer.find(".slide");
	
	var interval;
	
	function startSlider(){
		interval = setInterval(function(){
			$slideContainer.animate({"margin-left": "-="+width}, animationSpeed, function() {
				currentSlide++;	
				if (currentSlide === $slides.length) {
					currentSlide = 1;
					$slideContainer.css("margin-left", 0); 
				}
			});
		}, pause);
	}
	
	function stopSlider() {
		clearInterval(interval);
	}
	
	$slider.on("mouseenter", stopSlider).on("mouseleave", startSlider);
	
	startSlider();
		
	//listen for mouseenter and pause
	
	//resume on mouseleave
		   
		   
	var myInterval = setInterval(function(){
		//console.log(new Date());
	}, 1000);


	
});