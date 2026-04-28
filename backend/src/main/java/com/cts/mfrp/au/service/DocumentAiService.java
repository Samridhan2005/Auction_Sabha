package com.cts.mfrp.au.service;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DocumentAiService {

    public record AiVerdictResult(
        String verdict,
        String documentType,
        int confidence,
        String summary,
        List<String> concerns,
        String suggestions
    ) {}

    public AiVerdictResult verifyDocument(String documentUrl, String productName, String description) {
        if (documentUrl == null || documentUrl.isBlank()) {
            return result("SUSPICIOUS", "No Document", 95,
                "No document URL was provided by the seller.",
                List.of("Missing document URL"),
                "Contact the seller to upload a valid document before approving.");
        }

        String lower = documentUrl.toLowerCase().trim();

        if (isUrlShortener(lower)) {
            return result("SUSPICIOUS", "Shortened URL", 90,
                "The document URL uses a URL shortener. The actual destination cannot be verified.",
                List.of("URL shortener detected — destination is hidden", "Cannot confirm document type or source"),
                "Request the seller to provide a direct link (Google Drive / Dropbox) instead of a shortened URL.");
        }

        if (lower.contains("drive.google.com")) {
            return analyzeGoogleDriveUrl(lower);
        }

        if (lower.contains("dropbox.com")) {
            boolean isDirect = lower.contains("dl=1") || lower.contains("raw=1");
            return result("LIKELY_AUTHENTIC", "Cloud Document (Dropbox)", 76,
                "Document is hosted on Dropbox, a recognized cloud storage platform. " +
                (isDirect ? "Direct download link detected." : "URL structure appears valid."),
                List.of(),
                "Open 'View Documents' to manually verify the document contents match the product.");
        }

        if (lower.contains("onedrive.live.com") || lower.contains("1drv.ms") || lower.contains("sharepoint.com")) {
            return result("LIKELY_AUTHENTIC", "Cloud Document (Microsoft OneDrive)", 74,
                "Document is hosted on Microsoft OneDrive/SharePoint, a recognized enterprise cloud platform.",
                List.of(),
                "Open 'View Documents' to manually verify the document contents.");
        }

        if (lower.contains("docs.google.com")) {
            return result("LIKELY_AUTHENTIC", "Google Docs / Sheets", 70,
                "Document is a Google Docs file, which is accessible and verifiable online.",
                List.of("Ensure the document is not just a blank template"),
                "Open the link to confirm the document contains relevant authenticity information.");
        }

        boolean isPdf  = lower.contains(".pdf");
        boolean isImage = lower.contains(".jpg") || lower.contains(".jpeg") || lower.contains(".png") || lower.contains(".webp");
        boolean isDoc   = lower.contains(".doc") || lower.contains(".docx");

        if (isPdf || isDoc) {
            return result("NEEDS_REVIEW", isPdf ? "PDF Document" : "Word Document", 60,
                "The URL links directly to a document file, but is not hosted on a recognized cloud storage platform.",
                List.of("Document hosted on an unrecognized platform"),
                "Verify the hosting site is trustworthy. Open the link and check the document contents carefully.");
        }

        if (isImage) {
            return result("NEEDS_REVIEW", "Image File", 50,
                "The URL points to an image file. Images can be edited and may not constitute sufficient proof of authenticity.",
                List.of("Image format may be insufficient for authenticity proof", "Images can be altered or fabricated"),
                "Consider requesting a PDF certificate or an official scanned document in addition to this image.");
        }

        return result("NEEDS_REVIEW", "Unknown Document Type", 40,
            "The URL does not match any recognized document hosting pattern. Document type could not be determined.",
            List.of("Unrecognized document host", "Cannot determine document type from URL alone"),
            "Ask the seller to upload their documents to Google Drive and share a proper shareable link.");
    }

    private AiVerdictResult analyzeGoogleDriveUrl(String lower) {
        if (lower.contains("/folders/")) {
            return result("NEEDS_REVIEW", "Google Drive — Folder Link", 50,
                "The URL links to a Google Drive folder, not a specific file. Individual documents cannot be verified from a folder link.",
                List.of("Link points to a folder, not a specific document"),
                "Ask the seller to share a direct link to the specific file: right-click file → Share → Copy link.");
        }

        boolean hasFileId = lower.contains("/file/d/");
        boolean isProperShare = lower.contains("/view") || lower.contains("/preview") || lower.contains("usp=");

        if (hasFileId && isProperShare) {
            return result("LIKELY_AUTHENTIC", "Document (Google Drive)", 84,
                "The URL is a valid Google Drive file sharing link with a proper file ID and sharing parameters. Structure looks legitimate.",
                List.of(),
                "Open 'View Documents' to verify the document contents match the listed product.");
        }

        if (hasFileId) {
            return result("LIKELY_AUTHENTIC", "Document (Google Drive)", 72,
                "The URL contains a valid Google Drive file ID. Sharing parameters appear incomplete but the file may still be accessible.",
                List.of("Sharing parameters may be incomplete — file might require login to access"),
                "Try opening the link in a private browser. If it requires login, ask the seller to set sharing to 'Anyone with the link'.");
        }

        return result("NEEDS_REVIEW", "Google Drive — Non-Standard Link", 48,
            "The URL is from Google Drive but does not follow the standard file sharing format.",
            List.of("Non-standard Google Drive URL — file may not be publicly accessible"),
            "Ask the seller to reshare: open the file in Drive → Share → Anyone with the link → Copy link.");
    }

    private boolean isUrlShortener(String url) {
        return url.contains("bit.ly") || url.contains("tinyurl.com") || url.contains("t.co")
            || url.contains("goo.gl") || url.contains("ow.ly") || url.contains("buff.ly")
            || url.contains("short.link") || url.contains("rb.gy") || url.contains("cutt.ly");
    }

    private AiVerdictResult result(String verdict, String docType, int confidence,
                                    String summary, List<String> concerns, String suggestions) {
        return new AiVerdictResult(verdict, docType, confidence, summary, concerns, suggestions);
    }
}
