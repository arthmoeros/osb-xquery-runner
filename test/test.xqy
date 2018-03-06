xquery version "1.0" encoding "utf-8";

(:: OracleAnnotationVersion "1.0" ::)

declare namespace ns1="http://sample.origin/request";
(:: import schema at "SampleOriginRequest.xsd" ::)
declare namespace ns2="http://sample.target/request";
(:: import schema at "SampleTargetRequest.xsd" ::)

declare variable $chars3 as xs:string external;
declare variable $request as element() (:: schema-element(ns1:sampleOriginRequest) ::) external;
declare variable $number as xs:int external;
declare variable $chars2 as xs:string external;

declare function local:func($chars2 as xs:string,
							$request as element() (:: schema-element(ns1:sampleOriginRequest) ::),
							$number as xs:int,
							$chars3 as xs:string) as element() (:: schema-element(ns2:SampleTargetRequest) ::) {
    <ns2:SampleTargetRequest>
    	<passedRequest>{fn-bea:trim(data($request/testInput))}</passedRequest>
    	<passedNumber>{$number}</passedNumber>
    	<passedChars>{$chars3}</passedChars>
    	<passedChars>{$chars2}</passedChars>
    </ns2:SampleTargetRequest>
};

local:func($chars2,$request,$number,$chars3)
