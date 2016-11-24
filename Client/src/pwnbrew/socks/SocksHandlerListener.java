/*
 * IMPORTANT - READ CAREFULLY: This License Agreement is a legal agreement between You and Securifera, Inc. Read it carefully before completing the installation process and using the Software. It provides a license to use the Software and contains warranty information and liability disclaimers. BY INSTALLING, COPYING OR OTHERWISE USING THE SOFTWARE, YOU ARE CONFIRMING YOUR ACCEPTANCE OF THE SOFTWARE AND AGREEING TO BECOME BOUND BY THE TERMS OF THIS AGREEMENT. IF YOU DO NOT AGREE, DO NOT INSTALL OR USE THE PRODUCT. The Software is owned by Securifera, Inc and/or its licensors and is protected by copyright laws and international copyright treaties, as well as other intellectual property laws and treaties. THE SOFTWARE IS LICENSED, NOT SOLD.

 * DEFINITIONS.
 * "Vendor" means Securifera, Inc
 * "You", "Your" means you and your company.
 * "Software" means the product provided to You, which includes computer software and may include associated media, printed materials, and "online" or electronic documentation. 
 * OWNERSHIP. The Software is owned and copyrighted by Vendor and/or its licensors. Your license confers no title or ownership in the Software and is not a sale of any rights in the Software.
 * GRANT OF LICENSE. Vendor grants You the following rights provided You comply with all terms and conditions of this agreement. For each license You have acquired for the Software:
 * You are granted a non-exclusive right to use and install ONE copy of the software on ONE computer.
 * You may make one copy for backup or archival purposes.
 * You may modify the configuration files (if applicable).
 * RESTRICTED USE.
 * You agree to use reasonable efforts to prevent unauthorized copying of the Software.
 * You may not disable any licensing or control features of the Software or allow the Software to be used with such features disabled.
 * You may not share, rent, or lease Your right to use the Software.
 * You may not modify, sublicense, copy, rent, sell, distribute or transfer any part of the Software except as provided in this Agreement.
 * You may not reverse engineer, decompile, translate, create derivative works, decipher, decrypt, disassemble, or otherwise convert the Software to a more human-readable form for any reason.
 * You will return or destroy all copies of the Software if and when Your right to use it ends.
 * You may not use the Software for any purpose that is unlawful.
 * ADDITIONAL SOFTWARE This license applies to updates, upgrades, plug-ins and any other additions to the original Software provided by Vendor, unless Vendor provides other terms along with the additional software.
 * REGISTRATION. The software will electronically register itself during installation to confirm that You have entered a valid “License Key". The registration process only sends the license information that You've entered (License key) and information about the software installed (Program ID, Version, Checksum and selected Network Interface MAC address). No other information is sent.
 * UPGRADES. If this copy of the software is an upgrade from an earlier version of the software, it is provided to You on a license exchange basis. Your use of the Software upgrade is subject to the terms of this license, and You agree by Your installation and use of this copy of the Software to voluntarily terminate Your earlier license and that You will not continue to use the earlier version of the Software or transfer it to another person or entity.
 * TRANSFER. You cannot transfer the Software and Your rights under this license to another party.
 * SUBLICENSING. You may not sublicense the Software and Your rights under this license to another party
 * TERMINATION. Vendor may terminate Your license if You do not abide by the license terms or if You have not paid applicable license fees. Termination of the license may include, but not be limited to, marking the License Key as invalid to prevent further installations or usage. Upon termination of license, You shall immediately discontinue the use of the Software and shall within ten (10) days return to Vendor all copies of the Software or confirm that You have destroyed all copies of it. Your obligations to pay accrued charges and fees, if any, shall survive any termination of this Agreement. You agree to indemnify Vendor and its licensors for reasonable attorney fees in enforcing its rights pursuant to this license.
 * DISCLAIMER OF WARRANTY. The Software is provided on an "AS IS" basis, without warranty of any kind, including, without limitation, the warranties of merchantability, fitness for a particular purpose and non- infringement. The entire risk as to the quality and performance of the Software is borne by You. Should the Software prove defective, You, not Vendor or its licensors, assume the entire cost of any service and repair. If the Software is intended to link to, extract content from or otherwise integrate with a third party service, Vendor makes no representation or warranty that Your particular use of the Software is or will continue to be authorized by law in Your jurisdiction or that the third party service will continue to be available to You. This disclaimer of warranty constitutes an essential part of the agreement.
 * LIMITATION OF LIABILITY. UNDER NO CIRCUMSTANCES AND UNDER NO LEGAL THEORY, TORT, CONTRACT, OR OTHERWISE, SHALL VENDOR OR ITS LICENSORS BE LIABLE TO YOU OR ANY OTHER PERSON FOR ANY INDIRECT, SPECIAL, PUNITIVE, INCIDENTAL, OR CONSEQUENTIAL DAMAGES OF ANY CHARACTER INCLUDING, WITHOUT LIMITATION, DAMAGES FOR WORK STOPPAGE, COMPUTER FAILURE OR LOSS OF REVENUES, PROFITS, GOODWILL, USE, DATA OR OTHER INTANGIBLE OR ECONOMIC LOSSES. IN NO EVENT WILL VENDOR OR ITS LICENSORS BE LIABLE FOR ANY DAMAGES IN EXCESS OF THE AMOUNT PAID TO LICENSE THE SOFTWARE, EVEN IF YOU OR ANY OTHER PARTY SHALL HAVE INFORMED VENDOR OR ITS LICENSORS OF THE POSSIBILITY OF SUCH DAMAGES, OR FOR ANY CLAIM. NO CLAIM, REGARDLESS OF FORM, MAY BE MADE OR ACTION BROUGHT BY YOU MORE THAN ONE YEAR AFTER THE BASIS FOR THE CLAIM BECOMES KNOWN TO THE PARTY ASSERTING IT.
 * APPLICABLE LAW. This license shall be interpreted in accordance with the laws of the United States of America. Any disputes arising out of this license shall be adjudicated in a court of competent jurisdiction in the United States of America.
 * GOVERNING LANGUAGE. Any translation of this License is done for local requirements and in the event of a dispute between the English and any non-English versions, the English version of this License shall govern.
 * ENTIRE AGREEMENT. This license constitutes the entire agreement between the parties relating to the Software and supersedes any proposal or prior agreement, oral or written, and any other communication relating to the subject matter of this license. Any conflict between the terms of this License Agreement and any Purchase Order, invoice, or representation shall be resolved in favour of the terms of this License Agreement. In the event that any clause or portion of any such clause is declared invalid for any reason, such finding shall not affect the enforceability of the remaining portions of this License and the unenforceable clause shall be severed from this license. Any amendment to this agreement must be in writing and signed by both parties.

 */
package pwnbrew.socks;

/**
 *
 * @author user
 */
public interface SocksHandlerListener {
    
    public void removeHandler(int passedId );
    
    public int getChannelId();
    
}