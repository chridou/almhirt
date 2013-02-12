package almhirt.app

trait Splash {
  def startupMessage: String
  def goodByeMessage: String
  def crashMessage: String
}

trait DefaultSplash extends Splash {
  def startupSplash =
    """ |     ___       __      .___  ___.  __    __   __  .______     .___________.
		|    /   \     |  |     |   \/   | |  |  |  | |  | |   _  \    |           |
		|   /  ^  \    |  |     |  \  /  | |  |__|  | |  | |  |_)  |   `---|  |----`
		|  /  /_\  \   |  |     |  |\/|  | |   __   | |  | |      /        |  |     
		| /  _____  \  |  `----.|  |  |  | |  |  |  | |  | |  |\  \----.   |  |     
		|/__/     \__\ |_______||__|  |__| |__|  |__| |__| | _| `._____|   |__|
	    |
	    |www.almhirt.org
	    |""".stripMargin

  def goodbye =
    """ |  ___   __    __  ____  ____  _  _  ____ 
	    | / __) /  \  /  \(    \(  _ \( \/ )(  __)
		|( (_ \(  O )(  O )) D ( ) _ ( )  /  ) _) 
		| \___/ \__/  \__/(____/(____/(__/  (____)
    	|""".stripMargin

  def crash =
    """ |                               ________________ 
		|                          ____/ (  (    )   )  \___ 
		|                         /( (  (  )   _    ))  )   )\ 
		|                       ((     (   )(    )  )   (   )  ) 
		|                     ((/  ( _(   )   (   _) ) (  () )  ) 
		|                    ( (  ( (_)   ((    (   )  .((_ ) .  )_ 
		|                   ( (  )    (      (  )    )   ) . ) (   ) 
		|                  (  (   (  (   ) (  _  ( _) ).  ) . ) ) ( ) 
		|                  ( (  (   ) (  )   (  ))     ) _)(   )  )  ) 
		|                 ( (  ( \ ) (    (_  ( ) ( )  )   ) )  )) ( ) 
		|                  (  (   (  (   (_ ( ) ( _    )  ) (  )  )   ) 
		|                 ( (  ( (  (  )     (_  )  ) )  _)   ) _( ( ) 
		|                  ((  (   )(    (     _    )   _) _(_ (  (_ ) 
		|                   (_((__(_(__(( ( ( |  ) ) ) )_))__))_)___) 
		|                   ((__)        \\||lll|l||///          \_)) 
		|                            (   /(/ (  )  ) )\   ) 
		|                          (    ( ( ( | | ) ) )\   ) 
		|                           (   /(| / ( )) ) ) )) ) 
		|                         (     ( ((((_(|)_)))))     ) 
		|                          (      ||\(|(|)|/||     ) 
		|                        (        |(||(||)||||        )
		|                          (     //|/l|||)|\\ \     )
		|                        (/ / //  /|//||||\\  \ \  \ _) 
		|------------------------------------------------------------------------------- 
		| _  _    __    ____  _____  _____  _____  _____  __  __ /\ 
		|( )/ )  /__\  (  _ \(  _  )(  _  )(  _  )(  _  )(  \/  ))( 
		| )  (  /(__)\  ) _ < )(_)(  )(_)(  )(_)(  )(_)(  )    ( \/ 
		|(_)\_)(__)(__)(____/(_____)(_____)(_____)(_____)(_/\/\_)()
  		|""".stripMargin

}