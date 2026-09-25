import React from 'react';
import { useOutletContext } from 'react-router-dom';
import AgentXAssistant from '../../components/ui/AgentXAssistant';
import Card, { CardHeader, CardBody } from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import { Bot, Cpu, ShieldCheck, Sparkles, BookOpen, Clock, FileText, Wrench } from 'lucide-react';

export const StudentAssistantPage = () => {
  const { refreshData } = useOutletContext();

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Overview Banner for Multi-Agent AI */}
      <div className="p-5 rounded-2xl border border-[var(--color-border)] bg-gradient-to-br from-[var(--color-card)] to-[var(--color-background)] shadow-sm">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-start gap-3.5">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-500 to-violet-500 flex items-center justify-center text-white shadow-md shrink-0">
              <Bot className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-base font-bold text-[var(--color-foreground)] tracking-tight">
                  AgentX Autonomous Multi-Agent Command Center
                </h2>
                <Badge variant="primary" size="sm">Groq Llama 3.3 70B</Badge>
              </div>
              <p className="text-xs text-[var(--color-muted-foreground)] mt-1">
                Your intelligent assistant for academic records, schedules, policy citations, maintenance tickets, and AI task planning.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 text-[10px] text-[var(--color-muted-foreground)]">
            <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
            <span className="font-semibold text-emerald-600 dark:text-emerald-400">5 Specialized Agents Active</span>
          </div>
        </div>

        {/* Specialized Agents Pill Grid */}
        <div className="grid grid-cols-2 sm:grid-cols-5 gap-2.5 mt-4 pt-4 border-t border-[var(--color-border)]">
          <div className="p-2 rounded-lg bg-[var(--color-card)] border border-[var(--color-border)] text-center">
            <p className="text-[10px] font-bold text-[var(--color-foreground)]">Academic Agent</p>
            <p className="text-[9px] text-[var(--color-muted-foreground)]">Attendance & Grades</p>
          </div>
          <div className="p-2 rounded-lg bg-[var(--color-card)] border border-[var(--color-border)] text-center">
            <p className="text-[10px] font-bold text-[var(--color-foreground)]">Schedule Agent</p>
            <p className="text-[9px] text-[var(--color-muted-foreground)]">Classes & Events</p>
          </div>
          <div className="p-2 rounded-lg bg-[var(--color-card)] border border-[var(--color-border)] text-center">
            <p className="text-[10px] font-bold text-[var(--color-foreground)]">Document RAG</p>
            <p className="text-[9px] text-[var(--color-muted-foreground)]">Verified Regulations</p>
          </div>
          <div className="p-2 rounded-lg bg-[var(--color-card)] border border-[var(--color-border)] text-center">
            <p className="text-[10px] font-bold text-[var(--color-foreground)]">Task Planner</p>
            <p className="text-[9px] text-[var(--color-muted-foreground)]">Auto Study Tasks</p>
          </div>
          <div className="p-2 rounded-lg bg-[var(--color-card)] border border-[var(--color-border)] text-center">
            <p className="text-[10px] font-bold text-[var(--color-foreground)]">Support Agent</p>
            <p className="text-[9px] text-[var(--color-muted-foreground)]">Auto Ticket Filing</p>
          </div>
        </div>
      </div>

      {/* Main Multi-Agent Interactive Assistant Component */}
      <AgentXAssistant onActionCompleted={() => refreshData()} />
    </div>
  );
};

export default StudentAssistantPage;
