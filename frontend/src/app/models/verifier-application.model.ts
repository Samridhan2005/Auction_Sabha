export interface VerifierApplicationRequest {
  name: string;
  email: string;
  phone: string;
  age: number;
  qualification: string;
  experienceYears: number;
  domainsInterested: string;
  motivation: string;
  portfolioUrl?: string;
}

export interface VerifierApprovalResponse {
  application: VerifierApplication;
  tempPassword: string;
  emailSent: boolean;
}

export interface VerifierApplication {
  applicationId: number;
  name: string;
  email: string;
  phone: string;
  age: number;
  qualification: string;
  experienceYears: number;
  domainsInterested: string;
  motivation: string;
  portfolioUrl?: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  adminRemarks?: string;
  submittedAt: string;
}
